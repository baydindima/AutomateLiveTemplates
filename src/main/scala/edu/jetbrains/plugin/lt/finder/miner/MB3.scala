package edu.jetbrains.plugin.lt.finder.miner

import com.intellij.lang.ASTNode
import edu.jetbrains.plugin.lt.finder.common.Template
import edu.jetbrains.plugin.lt.finder.sstree.TemplateSearchConfiguration

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
  * Class implements MB3 algorithm.
  * It returns frequent induced trees in forest.
  *
  * @param minerConfiguration          parameters of MB3 algorithm
  * @param templateSearchConfiguration configuration for filter templates
  * @param templateFilter              filter of nodes
  * @param templateProcessor           processor of templates
  */
class MB3(val minerConfiguration: MinerConfiguration,
          val templateSearchConfiguration: TemplateSearchConfiguration,
          val templateFilter: FileTypeTemplateFilter,
          val templateProcessor: TemplateProcessor) {

  /**
    * Get templates of forest.
    *
    * @param roots root nodes of files
    * @return templates
    */
  def getTemplates(roots: Seq[ASTNode]): List[Template] = {
    val (dict, minSupport) = buildDictionary(roots)
    println(s"Dictionary length ${dict.length}")

    val treeList = getEncodingCandidates(minSupport, dict)

    treeList.map(templateProcessor.process).foreach {
      str =>
        println(str.text)
        println(if (templateSearchConfiguration.isPossibleTemplate(str)) "valid" else 'invalid)
        println("_______________________")
    }


    val templates = treeList.map(templateProcessor.process)
      .filter(templateSearchConfiguration.isPossibleTemplate)
      .groupBy(_.text).mapValues(_.head).values.toList.sortBy(-_.text.length)
    println(s"Tree count: ${treeList.size}")
    templates
  }

  /**
    * Builds dictionary from nodes.
    * Dictionary is array of all nodes in pre order traversal.
    *
    * @param roots root nodes of files
    * @return dictionary and min support count
    */
  private def buildDictionary(roots: Seq[ASTNode]): (ArrayBuffer[DictionaryNode], Int) = {
    val nodeIdToCount: mutable.Map[NodeId, Int] = mutable.Map.empty

    /**
      * Add occurrence count for node id
      *
      * @param nodeId node id
      */
    def addOccurrence(nodeId: NodeId): Unit =
      nodeIdToCount += (nodeId -> (nodeIdToCount.getOrElse(nodeId, 0) + 1))

    /**
      * Optimization to maintain only one instance of each node id
      */
    val nodeIdMap: mutable.Map[NodeId, NodeId] = mutable.Map.empty

    val result: ArrayBuffer[DictionaryNode] = new ArrayBuffer[DictionaryNode]()

    /**
      * Get node id by ast node and children count.
      *
      * @param astNode       ast node
      * @param childrenCount children count
      * @return node id
      */
    def getNodeId(astNode: ASTNode, childrenCount: Int): NodeId = {
      val nodeId = NodeId(astNode, childrenCount)
      nodeIdMap.getOrElseUpdate(nodeId, nodeId)
    }

    /**
      * DFS traversal by ast tree.
      *
      * @param curNode   current ast node
      * @param parentPos position of parent node in dictionary
      * @param curDepth  current depth in ast tree
      */
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

      addOccurrence(nodeId)

      val shouldAnalyze = templateFilter.shouldAnalyze(nodeId)

      val dictNode: DictionaryNode =
        if (shouldAnalyze) {
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

    roots.foreach { root =>
      dfs(root, -1, 0)
    }

    /**
      * Get frequent nodes.
      *
      * @param nodeIdToCount map node id to occurrence count
      * @return set of frequent nodes and min support
      */
    def getFreqNodes(nodeIdToCount: mutable.Map[NodeId, Int]): (Set[NodeId], Int) = {
      val nodeCount = nodeIdToCount.size

      val nodeOccurrenceCount = nodeIdToCount.values.sum
      val minSupport = ((nodeOccurrenceCount / nodeCount) * minerConfiguration.minSupportCoefficient).toInt
      println(s"Node count: $nodeCount")
      println(s"Node occurrence count: $nodeOccurrenceCount")
      println(s"Min support: $minSupport")

      (nodeIdToCount.filter(_._2 >= minSupport).keys.toSet, minSupport)
    }

    val (freqNodes, minSupport) = getFreqNodes(nodeIdToCount)

    result.transform {
      case node: Node =>
        if (freqNodes(node.nodeId)) node else new DictionaryPlaceholder(
          depth = node.depth,
          parentPos = node.parentPos)
      case d => d
    }

    (result, minSupport)
  }

  /**
    * Get tree encoding candidates.
    *
    * @param minSupport min count of occurrence encoding in tree
    * @param dictionary array of all nodes in pre order
    * @return tree encoding candidates
    */
  private def getEncodingCandidates(minSupport: Int,
                                    dictionary: ArrayBuffer[DictionaryNode]): List[TreeEncoding] = {
    val occurrenceMap = mutable.Map.empty[TreeEncoding, ListBuffer[Occurrence]]

    for (
      i <- dictionary.indices
    ) {
      dictionary(i) match {
        case node: Node => node.nodeId match {
          case inner: InnerNodeId =>
            val encode = TreeEncoding(List(EncodeNode(inner)))
            new Occurrence(i, i) +=: occurrenceMap.getOrElseUpdate(encode, new ListBuffer[Occurrence]())
          case _ =>
        }
        case _ =>
      }
    }

    def extendMap(occMap: mutable.Map[TreeEncoding, ListBuffer[Occurrence]]): List[TreeEncoding] = {
      occMap.par.flatMap { case (enc, encList) =>
        val (newCandidates, isTemplate) = extend(enc, encList, minSupport, dictionary)
        val result = extendMap(newCandidates)
        if (isTemplate) enc :: result else result
      }.toList
    }

    extendMap(occurrenceMap)
  }

  /**
    * Try to add new node to candidates.
    * And check current encoding has enough occurrence count in dictionary.
    *
    * @param prefixEncoding current prefix encoding.
    * @param occurrenceList list of all occurrence in dictionary of prefix
    * @param minSupport     min count of occurrence encoding in tree
    * @param dictionary     array of all nodes in pre order
    * @return map of encoding to occurrence list and flag that indicates whether to add to candidates the prefix encoding
    */
  private def extend(prefixEncoding: TreeEncoding,
                     occurrenceList: ListBuffer[Occurrence],
                     minSupport: Int,
                     dictionary: ArrayBuffer[DictionaryNode]): (mutable.Map[TreeEncoding, ListBuffer[Occurrence]], Boolean) = {
    val completedBuckets: mutable.HashMap[TreeEncoding, ListBuffer[Occurrence]] = mutable.HashMap.empty
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
                newOccurrence +=: completedBuckets(treeEncoding)
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
            completedBuckets += (enc -> filteredOccList)
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


    (completedBuckets.map { case (enc, occList) =>
      TreeEncoding(enc.encodeList ::: prefixEncoding.encodeList) -> occList
    }, totalCount - placedRoots.size >= minSupport)
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

  override def toString: String = DefaultTemplateProcessor.getTemplate(encodeList).text

}

trait PathNode

case class EncodeNode(nodeId: NodeId) extends PathNode

object Up extends PathNode

object Placeholder extends PathNode