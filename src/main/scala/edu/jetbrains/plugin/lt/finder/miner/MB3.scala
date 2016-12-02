package edu.jetbrains.plugin.lt.finder.miner

import com.intellij.lang.ASTNode
import edu.jetbrains.plugin.lt.finder.common.{Template, TemplateStatistic}
import edu.jetbrains.plugin.lt.finder.sstree.DefaultSearchConfiguration

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


class MB3 {

  var treeMap: List[TreeEncoding] = List.empty

  def getTemplates(roots: Seq[ASTNode], fileTypeTemplateFilter: FileTypeTemplateFilter): List[Template] = {
    val nodeOccurrence = getNodeOccurrence(roots)

    val nodeCount = nodeOccurrence.size

    val nodeOccurrenceCount = nodeOccurrence.values.sum
    val minSupport = (nodeOccurrenceCount / nodeCount) / 2

    println(s"Node count: $nodeCount")
    println(s"Node occurrence count: $nodeOccurrenceCount")
    println(s"Min support: $minSupport")

    val freqNodes = nodeOccurrence.filter(_._2 >= minSupport).keys.toSet
    println(s"Freq node count: ${freqNodes.size}")
    println(s"Freq node occurrence count: ${nodeOccurrence.filter { case (n, _) => freqNodes(n) }.values.sum}")

    val leaves = freqNodes.filter {
      case _: LeafNodeId => true
      case _ => false
    }

    println(s"Leaves count: ${leaves.size}")
    println(s"Leaves occurrence count: ${nodeOccurrence.filter { case (n, _) => leaves(n) }.values.sum}")

    val dict = buildDictionary(roots, freqNodes, fileTypeTemplateFilter)
    println(s"Dictionary length ${dict.length}")

    start(minSupport, dict)

    treeMap.map(_.getTemplate).foreach {
      str =>
        println(str.text)
        println(if (DefaultSearchConfiguration.isPossibleTemplate(str)) "valid" else 'invalid)
        println("_______________________")
    }


    val templates = treeMap.map(_.getTemplate).filter(DefaultSearchConfiguration.isPossibleTemplate)
    println(s"Tree count: ${treeMap.size}")
    templates
  }


  def getNodeOccurrence(roots: Seq[ASTNode]): mutable.Map[NodeId, Int] = {
    val nodeIdToCount: mutable.Map[NodeId, Int] = mutable.Map.empty

    def addOccurrence(nodeId: NodeId): Unit =
      nodeIdToCount += (nodeId -> (nodeIdToCount.getOrElse(nodeId, 0) + 1))

    def dfs(curNode: ASTNode): Unit = {
      def next(child: ASTNode, childrenCountAcc: Int): Int = child match {
        case null => childrenCountAcc
        case childNode =>
          dfs(childNode)
          next(childNode.getTreeNext, childrenCountAcc + 1)
      }

      val childrenCount = next(
        child = curNode.getFirstChildNode,
        childrenCountAcc = 0
      )

      val nodeId = NodeId(curNode, childrenCount)

      addOccurrence(nodeId)
    }

    roots.foreach(dfs)

    nodeIdToCount
  }

  def buildDictionary(roots: Seq[ASTNode], freqNodes: Set[NodeId], filter: FileTypeTemplateFilter): ArrayBuffer[DictionaryNode] = {
    /**
      * Optimization to maintain only one instance of each node id
      */
    val nodeIdMap: mutable.Map[NodeId, NodeId] = mutable.Map.empty

    val result: ArrayBuffer[DictionaryNode] = new ArrayBuffer[DictionaryNode]()

    def getNodeId(astNode: ASTNode, childrenCount: Int): NodeId = {
      val nodeId = NodeId(astNode, childrenCount)
      nodeIdMap.getOrElseUpdate(nodeId, nodeId)
    }

    def dfs(curNode: ASTNode, parentPos: Int, curDepth: Int): Unit = {
      val curPos = result.size

      def childCountNext(child: ASTNode, childrenCountAcc: Int): Int = child match {
        case null => childrenCountAcc
        case childNode => childCountNext(childNode.getTreeNext, childrenCountAcc + 1)
      }

      def dfsNext(child: ASTNode): Unit = child match {
        case null =>
        case childNode =>
          dfs(
            curNode = childNode,
            curDepth = curDepth + 1,
            parentPos = curPos)

          dfsNext(childNode.getTreeNext)
      }

      val childCount = childCountNext(curNode.getFirstChildNode, 0)

      val nodeId = getNodeId(curNode, childCount)

      val shouldAnalyze = filter.shouldAnalyze(nodeId)

      val dictNode: DictionaryNode =
        if (shouldAnalyze && freqNodes(nodeId)) {
        new Node(
          nodeId = nodeId,
          depth = curDepth,
          parentPos = parentPos)
      } else {
        new DictionaryPlaceholder(
          depth = curDepth,
          parentPos = parentPos)
      }

      result += dictNode
      if (shouldAnalyze) {
        dfsNext(curNode.getFirstChildNode)
      }

      dictNode.rightmostLeafPos = result.size - 1
    }

    var i = 0
    var start = System.currentTimeMillis()

    roots.foreach { root =>
      i += 1
      if (i % 1000 == 0) {
        println(s"Cur count $i")
        println(s"Time is ${System.currentTimeMillis() - start}")
        println(s"Free memory ${Runtime.getRuntime.freeMemory()}")
        start = System.currentTimeMillis()
      }

      dfs(root, -1, 0)
    }

    result
  }

  def start(minSupport: Int,
            dictionary: ArrayBuffer[DictionaryNode]): Unit = {
    var occurrenceMap = mutable.Map.empty[TreeEncoding, List[Occurrence]]

    for (
      i <- dictionary.indices
    ) {
      dictionary(i) match {
        case node: Node => node.nodeId match {
          case inner: InnerNodeId =>
            val encode = TreeEncoding(List(EncodeNode(inner)))
            occurrenceMap += (encode -> (new Occurrence(i, i) :: occurrenceMap.getOrElse(encode, List.empty)))
          case _ =>
        }
        case _ =>
      }
    }

    while (occurrenceMap.nonEmpty) {
      occurrenceMap = occurrenceMap.flatMap { case (enc, encList) =>
        extend2(enc, encList, minSupport, dictionary)
      }
    }

  }

  def extend2(prefixEncoding: TreeEncoding,
              occurrenceList: List[Occurrence],
              minSupport: Int,
              dictionary: ArrayBuffer[DictionaryNode]): mutable.Map[TreeEncoding, List[Occurrence]] = {
    val completedBuckets: mutable.HashMap[TreeEncoding, List[Occurrence]] = mutable.HashMap.empty
    var uncompletedBuckets: mutable.LinkedHashMap[TreeEncoding, ListBuffer[Occurrence]] = mutable.LinkedHashMap.empty

    val unplacedRoots: mutable.HashSet[Int] = mutable.HashSet.empty
    occurrenceList.foreach(o => unplacedRoots += o.rootPos)
    val totalCount = unplacedRoots.size

    val placedRoots: mutable.HashSet[Int] = mutable.HashSet.empty

    val rightPosMap = mutable.Map.empty[Int, Int]
    occurrenceList.foreach(o => rightPosMap += (o.rootPos -> o.rightLeafPos))

    var first = true

    def next(rootPos: Int): Unit = {
      val rightmostLeaf = dictionary(rootPos).rightmostLeafPos
      var found = false
      var pos = rightPosMap(rootPos)
      var maxDepth = dictionary(pos).depth + 1
      while (!found && pos < rightmostLeaf) {
        pos += 1
        val dictNode = dictionary(pos)
        val depth = dictNode.depth
        if (depth <= maxDepth) {
          maxDepth = depth
          dictNode match {
            case node: Node =>
              found = true
              val treeEncoding = TreeEncoding(EncodeNode(node.nodeId) :: (if (first && rightPosMap(rootPos) == pos - 1) List.empty else List(Placeholder)))
              val newOccurrence = new Occurrence(rootPos, pos)

              if (completedBuckets.contains(treeEncoding)) {
                unplacedRoots -= rootPos
                placedRoots += rootPos
                completedBuckets += (treeEncoding -> (newOccurrence :: completedBuckets.getOrElse(treeEncoding, List.empty)))
              } else {
                rightPosMap += (newOccurrence.rootPos -> pos)
                uncompletedBuckets.getOrElseUpdate(treeEncoding, {
                  new ListBuffer[Occurrence]()
                }) += newOccurrence
              }
            case _ =>
          }
        }
      }
      if (!found) {
        unplacedRoots -= rootPos
      }
    }

    while (unplacedRoots.size >= minSupport) {
      unplacedRoots.foreach(next)
      val newUncompletedBuckets: mutable.LinkedHashMap[TreeEncoding, ListBuffer[Occurrence]] = mutable.LinkedHashMap.empty
      uncompletedBuckets.foreach { case (enc, occList) =>
        if (occList.size >= minSupport) {
          val filteredOccList = occList.filterNot(o => placedRoots.contains(o.rootPos))
          if (filteredOccList.size >= minSupport) {
            filteredOccList.foreach { o =>
              placedRoots += o.rootPos
              unplacedRoots -= o.rootPos
            }
            completedBuckets += (enc -> filteredOccList.toList)
          } else {
            newUncompletedBuckets += (enc -> filteredOccList)
          }
        } else {
          newUncompletedBuckets += (enc -> occList)
        }
      }
      first = false
      uncompletedBuckets = newUncompletedBuckets
    }

    if (totalCount - placedRoots.size >= minSupport) {
      treeMap ::= prefixEncoding
    }

    completedBuckets.map { case (enc, occList) =>
      TreeEncoding(enc.encodeList ::: prefixEncoding.encodeList) -> occList
    }
  }

  def getEdgeOccurrence(roots: Seq[ASTNode], freqNodes: Set[NodeId]): mutable.Map[(NodeId, NodeId), Int] = {
    val edgeToCount: mutable.Map[(NodeId, NodeId), Int] = mutable.Map.empty

    def addOccurrence(edge: (NodeId, NodeId)): Unit =
      edgeToCount += (edge -> (edgeToCount.getOrElse(edge, 0) + 1))

    def dfs(curNode: ASTNode): Option[NodeId] = {
      def next(child: ASTNode, childrenNodeAcc: Seq[NodeId], childrenCountAcc: Int): (Seq[NodeId], Int) = child match {
        case null => (childrenNodeAcc, childrenCountAcc)
        case childNode =>
          dfs(childNode) match {
            case Some(node) => next(childNode.getTreeNext, node +: childrenNodeAcc, childrenCountAcc + 1)
            case None => next(childNode.getTreeNext, childrenNodeAcc, childrenCountAcc + 1)
          }
      }

      val (nodes, childrenCount) = next(
        child = curNode.getFirstChildNode,
        childrenNodeAcc = List.empty,
        childrenCountAcc = 0
      )

      val nodeId = NodeId(curNode, childrenCount)

      if (freqNodes(nodeId)) {

        nodes
          .foreach(n => addOccurrence(nodeId, n))

        Some(nodeId)
      } else {
        None
      }
    }

    roots.foreach(dfs)

    edgeToCount
  }

  def extend(prefixEncoding: TreeEncoding,
             occurrenceList: List[Occurrence],
             minSupport: Int,
             dictionary: ArrayBuffer[DictionaryNode]): mutable.Map[TreeEncoding, List[Occurrence]] = {
    /** result of extend */
    var result = mutable.Map.empty[TreeEncoding, List[Occurrence]]

    /** store right position for occurrence */
    val rightPosMap = mutable.Map.empty[Int, Int]
    occurrenceList.foreach { occurrence => rightPosMap += (occurrence.rootPos -> occurrence.rightLeafPos) }

    /** store buf result */
    var occurrenceMap = mutable.LinkedHashMap.empty[TreeEncoding, List[Occurrence]]

    /** Count of occurrence */
    val occurrenceCount = occurrenceList.length

    /** Active occurrence */
    var occurrences: Set[Occurrence] = occurrenceList.toSet
    /** is first iteration */
    var first = true

    val usedRoots = new mutable.HashSet[Int]()

    while (occurrences.size >= minSupport) {
      var newOccurrenceSet: Set[Occurrence] = Set.empty

      occurrences.foreach { occurrence =>
        val rightmostLeaf = dictionary(occurrence.rootPos).rightmostLeafPos
        var found = false
        var pos = rightPosMap(occurrence.rootPos)
        var maxDepth = dictionary(pos).depth + 1
        while (!found && pos < rightmostLeaf) {
          pos += 1
          val dictNode = dictionary(pos)
          val depth = dictNode.depth
          if (depth <= maxDepth) {
            maxDepth = depth
            dictNode match {
              case node: Node =>
                found = true
                val newOccurrence = new Occurrence(occurrence.rootPos, pos)

                val treeEncoding = TreeEncoding(EncodeNode(node.nodeId) :: (if (first && rightPosMap(newOccurrence.rootPos) == pos - 1) List.empty else List(Placeholder)))
                rightPosMap += (newOccurrence.rootPos -> pos)
                occurrenceMap += (treeEncoding -> (newOccurrence :: occurrenceMap.getOrElse(treeEncoding, List.empty)))
              case _ =>
            }
          }
        }
        if (found) {
          newOccurrenceSet += occurrence
        }
      }

      occurrenceMap = occurrenceMap.flatMap { case (enc, encList) =>
        val list = encList.filterNot(n => usedRoots.contains(n.rootPos))
        val size = list.size
        if (size >= minSupport) {
          usedRoots ++= list.map(_.rootPos)
          result += (enc -> (list ::: result.getOrElse(enc, List.empty)))
          list.foreach(newOccurrenceSet -= _)
          List.empty
        } else {
          List(enc -> list)
        }
      }

      occurrences = newOccurrenceSet
      first = false
    }

    result = result.map { case (enc, encList) =>
      enc -> (encList ::: occurrenceMap.getOrElse(enc, List.empty))
    }

    val notExtendedCount = occurrenceCount - result.values.map(_.size).sum

    if (notExtendedCount >= minSupport) {
      treeMap ::= prefixEncoding
    }

    result.map { case (enc, encList) =>
      TreeEncoding(enc.encodeList ::: prefixEncoding.encodeList) -> encList
    }
  }
}

class Occurrence(val rootPos: Int,
                 val rightLeafPos: Int) {
  override def toString: String = s"{root: $rootPos, right: $rightLeafPos}"
}

class DictionaryNode(val depth: Int,
                     val parentPos: Int,
                     var rightmostLeafPos: Int) {

}


class Node(val nodeId: NodeId,
           override val depth: Int,
           override val parentPos: Int) extends DictionaryNode(depth, parentPos, -1) {
  override def toString = s"Node($nodeId, $depth, $parentPos)"
}

class DictionaryPlaceholder(override val depth: Int,
                            override val parentPos: Int) extends DictionaryNode(depth, parentPos, -1) {

  override def toString = s"DictionaryPlaceholder($depth, $parentPos)"
}

case class TreeEncoding(encodeList: List[PathNode]) {
  def getText: String = getCompressedEncodeList.map {
    case EncodeNode(leaf: LeafNodeId) => leaf.nodeText
    case Placeholder => " #_# "
    case _ => ""
  }.mkString

  private def getCompressedEncodeList: List[PathNode] = {
    def helper(encodeList: List[PathNode], prevIsPlaceholder: Boolean, accList: List[PathNode]): List[PathNode] = encodeList match {
      case node :: tail =>
        node match {
          case EncodeNode(leaf: LeafNodeId) =>
            if (leaf.nodeText.matches("(\\s|\\n)*"))
              if (prevIsPlaceholder)
                helper(tail, prevIsPlaceholder, accList)
              else
                helper(tail, prevIsPlaceholder, node :: accList)
            else
              helper(tail, prevIsPlaceholder = false, node :: accList)
          case EncodeNode(_: InnerNodeId) =>
            helper(tail, prevIsPlaceholder, accList)
          case Placeholder =>
            if (prevIsPlaceholder)
              helper(tail, prevIsPlaceholder = true, accList)
            else
              helper(tail, prevIsPlaceholder = true, node :: accList)
        }
      case Nil => accList
    }

    helper(encodeList, prevIsPlaceholder = false, List.empty)
  }

  override def toString: String = getTemplate.text

  def getTemplate: Template = {
    var placeholderCount = 0
    var nodeCount = 0
    val text = getCompressedEncodeList.map {
      case EncodeNode(leaf: LeafNodeId) =>
        nodeCount += 1
        leaf.nodeText
      case Placeholder =>
        placeholderCount += 1
        " #_# "
      case _ => ""
    }.mkString
    new Template(text, new TemplateStatistic(placeholderCount, nodeCount))
  }

}

trait PathNode

case class EncodeNode(nodeId: NodeId) extends PathNode

object Up extends PathNode

object Placeholder extends PathNode