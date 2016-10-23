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
          case (id, data) ⇒ throw new RuntimeException(s"Incompatible types of id and data $id, $data")
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
          (d.children.length, d.children.map(alter ⇒ alter.alternatives.size).sum)).foldLeft((0, 0)) {
          case (p1, p2) ⇒ (p1._1 + p2._1, p1._2 + p2._2)
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
    val (node, _) = add(astNode, CommonNodeStatistic.empty)

    node.data.addDifferentParentCount()
    rootNodes += node
  }

  /**
    * Add new node to STree,
    * update <code>idToMap</code>,
    * calculate statistic of node
    *
    * @param astNode             current node
    * @param commonNodeStatistic common statistic of current node (calculated in parent)
    * @return statistic of current node
    */
  private def add(astNode: ASTNode,
                  commonNodeStatistic: CommonNodeStatistic): (SimNode, NodeStatistic) = {
    val (childrenCount, children) = getChildren(astNode)
    val nodeId = NodeId(astNode, childrenCount)

    val data = putIfAbsentToIdToDataMap(nodeId, childrenCount)

    val simNode = SimNode(nodeId, data)

    val stat = simNode match {
      case node: SimInnerNode ⇒
        val childrenCommonNodeStatistic = CommonNodeStatistic(commonNodeStatistic, childrenCount - 1)

        val childrenStat = children.zip(node.data.children).map {
          case (child, alternatives) ⇒
            val (childNode, childStat) = add(child, childrenCommonNodeStatistic)

            if (alternatives.putIfAbsent(childNode)) {
              childNode.data.addDifferentParentCount()
            }

            childStat
        }

        val stat = InnerNodeStatistic(childrenStat, commonNodeStatistic)
        node.data.statistics += stat
        stat
      case node: SimLeafNode ⇒
        val stat = LeafNodeStatistic(commonNodeStatistic, node)
        node.data.statistics += stat
        stat
    }

    (simNode, stat)
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
      val data = SimNodeData(childrenCount)
      data.addOccurrence()
      idToData.put(nodeId, data)
      data
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

}
