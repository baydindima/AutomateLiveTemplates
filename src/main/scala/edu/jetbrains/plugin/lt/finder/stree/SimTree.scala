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
    * Set of all nodes from which to start adding
    */
  val rootNodes: mutable.Set[SimNode] = mutable.Set.empty

  /**
    * Map to store node id → data
    */
  val idToData: mutable.Map[NodeId, SimNodeData] = mutable.Map.empty


  /**
    * Add new AST tree to STree
    *
    * @param astNode root node
    */
  def add(astNode: ASTNode): Unit = {
    add(astNode, None, CommonNodeStatistic.empty)
  }

  /**
    * Add new node to STree
    *
    * @param astNode              current node
    * @param alternativesOfParent option alternatives of parent node
    */
  private def add(astNode: ASTNode,
                  alternativesOfParent: Option[NodeChildrenAlternatives[SimNode]],
                  commonNodeStatistic: CommonNodeStatistic): NodeStatistic = {
    val (childrenCount, children) = getChildren(astNode)
    val nodeId = getId(astNode, childrenCount)

    val data = updateIdToDataMap(nodeId, childrenCount)

    val simNode = alternativesOfParent match {
      case Some(p) ⇒
        updateAlternativesOfParent(p, nodeId, data)
      case None ⇒
        val simNode = SimNode(nodeId, data)
        rootNodes += simNode
        simNode
    }

    calcStatistic(simNode,
      commonNodeStatistic,
      children,
      childrenCount)
  }

  private def calcStatistic(simNode: SimNode,
                            commonNodeStatistic: CommonNodeStatistic,
                            children: Seq[ASTNode],
                            childrenCount: Int): NodeStatistic = {
    simNode match {
      case node: SimInnerNode ⇒
        val childrenCommonNodeStatistic = calcCommonStatistic(commonNodeStatistic, childrenCount)
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
      minHeight = leafStat.headOption.map(_ ⇒ 1).getOrElse(innerStat.map(_.minHeight).min),
      averageHeight = (leafStat.size + innerStat.map(_.averageHeight + 1).sum) / childrenStat.size,
      commonStatistic = commonNodeStatistic
    )
  }

  private def calcLeafStatistic(nodeStatistic: CommonNodeStatistic,
                                node: SimLeafNode): LeafNodeStatistic =
    new LeafNodeStatistic(
      textLength = node.nodeId.nodeText.value.length,
      commonStatistic = nodeStatistic
    )

  private def calcCommonStatistic(parentCommonStatistic: CommonNodeStatistic, siblingsCount: Int) =
    new CommonNodeStatistic(
      depth = parentCommonStatistic.depth + 1,
      siblingsCount = siblingsCount
    )

  private def updateAlternativesOfParent(alternativesOfParent: NodeChildrenAlternatives[SimNode],
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
  private def updateIdToDataMap(nodeId: NodeId, childrenCount: Int): SimNodeData = idToData.get(nodeId) match {
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
