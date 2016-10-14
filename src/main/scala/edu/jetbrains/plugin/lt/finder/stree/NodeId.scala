package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.psi.tree.IElementType

import scala.collection.mutable

/**
  * Base class for node's id
  * Id should describe node, using in test for equality
  */
sealed abstract class NodeId() {
  def elementType: ElementType
}

/**
  * Identifier of node which has children
  *
  * @param elementType   node type
  * @param childrenCount count of children
  */
case class InnerNodeId(elementType: ElementType,
                       childrenCount: ChildrenCount) extends NodeId

/**
  * Identifier of leaf node
  *
  * @param elementType node type
  * @param nodeText    text of node
  */
case class LeafNodeId(elementType: ElementType,
                      nodeText: NodeText) extends NodeId

/**
  * Value-class for storing children count
  */
case class ChildrenCount(value: Int) extends AnyVal


/**
  * Value-class for storing text of leaf node
  */
case class NodeText(value: String) extends AnyVal


/**
  * Value-class for storing ast node type
  */
case class ElementType(value: IElementType) extends AnyVal


/**
  * Class for representing alternatives of children
  *
  * @param alternatives map which store info about node 2 link
  */
class NodeChildrenAlternatives[T](val alternatives: mutable.Map[NodeId, T])

object NodeChildrenAlternatives {
  def apply[T](): NodeChildrenAlternatives[T] = new NodeChildrenAlternatives[T](new mutable.HashMap[NodeId, T]())
}