package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.lang.ASTNode

import scala.collection.mutable

/**
  * Class for representing generalized AST tree
  * Each node of tree stores occurrence count
  * And alternatives of children if it is a non leaf node and text otherwise
  * Created by Dmitriy Baidin.
  */
class SimTree {

  /**
    * Set of all nodes from which starts adding
    */
  val rootNodes: mutable.Set[SimNode] = mutable.Set.empty

  /**
    * Map to store node id → data
    */
  val idToData: mutable.Map[NodeId, SimNodeData] = mutable.Map.empty

  /**
    * Count of all tree which was added
    */
  var treeCount: Int = 0

  /**
    * Calculate statistic of generalized AST-Tree
    *
    * @return statistic of generalized AST-Tree
    */
  def calcTreeStatistic: SimTreeStatistic = {
    val (leafNodeData, innerNodeData): (Seq[SimLeafNodeData], Seq[SimInnerNodeData]) =
      ((List.empty[SimLeafNodeData], List.empty[SimInnerNodeData]) /: idToData) {
        case ((ls, is), s) ⇒ s match {
          case (id: InnerNodeId, data: SimInnerNodeData) ⇒
            (ls, data :: is)
          case (id: LeafNodeId, data: SimLeafNodeData) ⇒
            (data :: ls, is)
        }
      }
    val nodeCount = idToData.size
    val occurrenceCount = idToData.map(_._2.getOccurrenceCount).sum
    new SimTreeStatistic(
      countOfTrees = treeCount,
      countOfRoots = rootNodes.size,
      innerNodeCount = innerNodeData.size,
      occurrenceCountOfInnerNode = innerNodeData.map(_.getOccurrenceCount).sum,
      leavesNodeCount = leafNodeData.size,
      occurrenceCountOfLeafNode = leafNodeData.map(_.getOccurrenceCount).sum,
      nodeCount = nodeCount,
      occurrenceCountOfNode = occurrenceCount,
      maxCountOfAlternatives = innerNodeData.map(d ⇒ d.children.map(alter ⇒ alter.alternatives.size).max).max,
      averageCountOfAlternatives = {
        val (count, sum) = innerNodeData.map(d ⇒
          (d.children.length, d.children.map(alter ⇒ alter.alternatives.size).sum)).reduce { case (p1, p2) ⇒
          (p1._1 + p2._1, p1._2 + p2._2)
        }
        sum / count.toDouble
      },
      maxOccurrenceCount = idToData.map(_._2.getOccurrenceCount).max,
      averageOccurrenceCount = occurrenceCount / nodeCount.toDouble
    )
  }


  /**
    * Add new AST tree to STree
    *
    * @param astNode root node
    */
  def add(astNode: ASTNode): Unit = {
    treeCount += 1
    add(astNode, None, CommonNodeStatistic.empty)
  }

  /**
    * Add new node to STree,
    * update <code>idToMap</code>,
    * calculate statistic of node
    *
    * @param astNode              current node
    * @param alternativesOfParent option alternatives of parent node
    * @param commonNodeStatistic  common statistic of current node (calculated in parent)
    * @return statistic of current node
    */
  private def add(astNode: ASTNode,
                  alternativesOfParent: Option[NodeChildrenAlternatives[SimNode]],
                  commonNodeStatistic: CommonNodeStatistic): NodeStatistic = {
    val (childrenCount, children) = getChildren(astNode)
    val nodeId = getId(astNode, childrenCount)

    val data = putIfAbsentToIdToDataMap(nodeId, childrenCount)

    val simNode = alternativesOfParent match {
      case Some(p) ⇒
        putIfAbsentToAlternatives(p, nodeId, data)
      case None ⇒
        data.addDifferentParentCount()
        val simNode = SimNode(nodeId, data)
        rootNodes += simNode
        simNode
    }

    calcStatistic(simNode,
      commonNodeStatistic,
      children,
      childrenCount)
  }


  /**
    * Common method for calculating statistic,
    * for leaf nodes just calculate statistic,
    * for inner nodes calculate statistic in children nodes
    *
    * @param simNode             current node
    * @param commonNodeStatistic common statistic
    * @param children            children of current node
    * @param childrenCount       count of children
    * @return statistic of node
    */
  private def calcStatistic(simNode: SimNode,
                            commonNodeStatistic: CommonNodeStatistic,
                            children: Seq[ASTNode],
                            childrenCount: Int): NodeStatistic = {
    simNode match {
      case node: SimInnerNode ⇒
        val childrenCommonNodeStatistic = calcCommonStatistic(commonNodeStatistic, childrenCount - 1)
        val childrenStat = children.zip(node.data.children).map {
          case (child, alternatives) ⇒
            add(child, Some(alternatives), childrenCommonNodeStatistic)
        }
        val stat = calcInnerNodeStatistic(node, childrenStat, commonNodeStatistic)
        node.data.statistics += stat
        stat
      case node: SimLeafNode ⇒
        val stat = calcLeafStatistic(commonNodeStatistic, node)
        node.data.statistics += stat
        stat
    }
  }

  /**
    * Calculate statistic for inner node
    *
    * @param node                current node
    * @param childrenStat        statistics of children nodes
    * @param commonNodeStatistic common statistic
    * @return node's statistic
    */
  private def calcInnerNodeStatistic(node: SimInnerNode,
                                     childrenStat: Seq[NodeStatistic],
                                     commonNodeStatistic: CommonNodeStatistic): InnerNodeStatistic = {
    val (leafStat, innerStat): (Seq[LeafNodeStatistic], Seq[InnerNodeStatistic]) =
      ((List.empty[LeafNodeStatistic], List.empty[InnerNodeStatistic]) /: childrenStat) {
        case ((ls, is), s) ⇒ s match {
          case stat: InnerNodeStatistic ⇒
            (ls, stat :: is)
          case stat: LeafNodeStatistic ⇒
            (stat :: ls, is)
        }
      }
    new InnerNodeStatistic(
      nodeCount = leafStat.size + innerStat.size + innerStat.map(_.nodeCount).sum,
      leafCount = leafStat.size + innerStat.map(_.leafCount).sum,
      innerCount = innerStat.size + innerStat.map(_.innerCount).sum,
      maxDegreeSubtree = childrenStat.size max innerStat.map(_.maxDegreeSubtree).reduceOption(_ max _).getOrElse(0),
      maxHeight = innerStat.map(_.maxHeight).reduceOption(_ max _).getOrElse(0) + 1,
      minHeight = leafStat.headOption.map(_ ⇒ 0).getOrElse(innerStat.map(_.minHeight).min) + 1,
      averageHeight = (leafStat.size + innerStat.map(_.averageHeight + 1).sum) / childrenStat.size.toDouble,
      commonStatistic = commonNodeStatistic
    )
  }

  /**
    * Calculate statistic for leaf node
    *
    * @param nodeStatistic common statistic
    * @param node          current node
    * @return node's statistic
    */
  private def calcLeafStatistic(nodeStatistic: CommonNodeStatistic,
                                node: SimLeafNode): LeafNodeStatistic =
  new LeafNodeStatistic(
    textLength = node.nodeId.nodeText.value.length,
    commonStatistic = nodeStatistic
  )

  /**
    * Calculate common statistic of current node
    *
    * @param parentCommonStatistic common statistic of parent
    * @param siblingsCount         count of siblings
    * @return common statistic of current node
    */
  private def calcCommonStatistic(parentCommonStatistic: CommonNodeStatistic, siblingsCount: Int) =
  new CommonNodeStatistic(
    depth = parentCommonStatistic.depth + 1,
    siblingsCount = siblingsCount
  )

  /**
    * Add current node to parent's alternatives
    * if such node absent,
    * or just return already associated with parent node
    *
    * @param alternativesOfParent parent's children alternatives
    * @param nodeId               id of current node
    * @param data                 data of current node
    * @return node bounded with parent node
    */
  private def putIfAbsentToAlternatives(alternativesOfParent: NodeChildrenAlternatives[SimNode],
                                        nodeId: NodeId,
                                        data: SimNodeData): SimNode = {
    alternativesOfParent.alternatives.get(nodeId) match {
      case Some(node) ⇒ node
      case None ⇒
        data.addDifferentParentCount()
        val simNode = SimNode(nodeId, data)
        alternativesOfParent.alternatives.put(nodeId, simNode)
        simNode
    }
  }

  /**
    * Update id to data map,
    * Put if absent id → data
    *
    * @param nodeId        id of node
    * @param childrenCount count of children of this node
    * @return data of node
    */
  private def putIfAbsentToIdToDataMap(nodeId: NodeId, childrenCount: Int): SimNodeData = idToData.get(nodeId) match {
    case Some(data) ⇒
      data.addOccurrence()
      data
    case None ⇒
      val data = buildData(childrenCount)
      idToData.put(nodeId, data)
      data
  }

  /**
    * Build data of node
    *
    * @param childrenCount children count of node
    * @return [[SimLeafNodeData]] if children count is 0,
    *         or [[SimInnerNodeData]] with array size of <code>childrenCount</code> otherwise
    */
  private def buildData(childrenCount: Int): SimNodeData = childrenCount match {
    case 0 ⇒
      SimLeafNodeData.empty
    case n ⇒
      SimInnerNodeData.empty(childrenCount)
  }

  /**
    * Get children and them count
    *
    * @param astNode AST node
    * @return tuple of children count and list of children
    */
  private def getChildren(astNode: ASTNode): (Int, List[ASTNode]) = {
    var child = astNode.getFirstChildNode

    var childrenCount = 0
    var result: List[ASTNode] = List.empty

    while (child != null) {
      childrenCount += 1
      result ::= child
      child = child.getTreeNext
    }

    (childrenCount, result.reverse)
  }

  /**
    * Build id of AST node
    *
    * @param astNode       AST node
    * @param childrenCount count of children
    * @return id of node
    */
  private def getId(astNode: ASTNode, childrenCount: Int): NodeId = childrenCount match {
    case 0 ⇒
      LeafNodeId(
        ElementType(astNode.getElementType),
        NodeText(astNode.getText))
    case n ⇒
      InnerNodeId(
        ElementType(astNode.getElementType),
        ChildrenCount(n)
      )
  }


}
