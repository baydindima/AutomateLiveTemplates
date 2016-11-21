package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType

import scala.collection.mutable

/**
  * Base class for node's id
  * Id should describe node, using in test for equality
  */
sealed abstract class NodeId() {
  def elementType: IElementType
}

object NodeId {
  def apply(astNode: ASTNode, childrenCount: Int): NodeId = childrenCount match {
    case 0 =>
      LeafNodeId(
        astNode.getElementType,
        astNode.getText.replaceAll(" +", " "))
    case n =>
      InnerNodeId(
        astNode.getElementType,
        n
      )
  }
}

/**
  * Identifier of node which has children
  *
  * @param elementType   node type
  * @param childrenCount count of children
  */
case class InnerNodeId(elementType: IElementType,
                       childrenCount: Int) extends NodeId

/**
  * Identifier of leaf node
  *
  * @param elementType node type
  * @param nodeText    text of node
  */
case class LeafNodeId(elementType: IElementType,
                      nodeText: String) extends NodeId

/**
  * Class for representing alternatives of children
  *
  * @param alternatives map which store info about node 2 link
  */
class NodeChildrenAlternatives(val alternatives: mutable.Map[NodeId, SimNode],
                               val alternativeFrequency: mutable.Map[NodeId, Int]) {
  def putIfAbsent(node: SimNode): Boolean = {
    alternatives.get(node.nodeId) match {
      case Some(n) =>
        alternativeFrequency(node.nodeId) = alternativeFrequency(node.nodeId) + 1
        false
      case None =>
        alternativeFrequency.put(node.nodeId, 1)
        alternatives.put(node.nodeId, node)
        true
    }
  }
}

object NodeChildrenAlternatives {
  def apply(): NodeChildrenAlternatives = new NodeChildrenAlternatives(
    new mutable.HashMap[NodeId, SimNode](),
    new mutable.HashMap[NodeId, Int]())
}