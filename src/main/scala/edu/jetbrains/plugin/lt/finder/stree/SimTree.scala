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
  val rootNodes: mutable.Set[SimNodeId] = mutable.Set.empty

  /**
    * Map to store node id → data
    */
  val idToData: mutable.Map[SimNodeId, SimNodeData] = mutable.Map.empty


  /**
    * Add new AST tree to STree
    *
    * @param astNode root node
    */
  def add(astNode: ASTNode): Unit = {
    add(astNode, None)
  }

  /**
    * Add new node to STree
    *
    * @param astNode              current node
    * @param alternativesOfParent option alternatives of parent node
    */
  private def add(astNode: ASTNode, alternativesOfParent: Option[SNodeChildrenAlternatives]): Unit = {
    val (childrenCount, children) = getChildren(astNode)
    val nodeId = getId(astNode, childrenCount)

    val data = updateIdToDataMap(nodeId, childrenCount)

    alternativesOfParent match {
      case Some(p) ⇒
        p.alternatives.get(nodeId) match {
          case Some(info) ⇒
          case None ⇒
            p.alternatives.put(nodeId, data)
        }
      case None ⇒
        rootNodes += nodeId
    }

    data match {
      case data: SimInnerNodeData ⇒
        children.zip(data.children).foreach {
          case (child, alternatives) ⇒
            add(child, Some(alternatives))
        }
      case _ ⇒
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
  private def updateIdToDataMap(nodeId: SimNodeId, childrenCount: Int): SimNodeData = idToData.get(nodeId) match {
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
      new SimLeafNodeData
    case n ⇒
      new SimInnerNodeData(Array.fill(n) {
        SNodeChildrenAlternatives()
      })
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
  private def getId(astNode: ASTNode, childrenCount: Int): SimNodeId = childrenCount match {
    case 0 ⇒
      SimLeafNodeId(
        ElementType(astNode.getElementType),
        NodeText(astNode.getText))
    case n ⇒
      SimInnerNodeId(
        ElementType(astNode.getElementType),
        ChildrenCount(n)
      )
  }


}
