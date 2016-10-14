package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.psi.tree.IElementType

import scala.collection.mutable

/**
  * Base class for links, also stores occurrence of node
  */
sealed abstract class SimNodeData() {
  private var occurrenceCount: Int = 1

  def getOccurrenceCount: Int = occurrenceCount

  def addOccurrence(): Unit = {
    occurrenceCount += 1
  }

}

/**
  * Data to children of leaf node, stores only occurrence count
  */
class SimLeafNodeData extends SimNodeData

/**
  * Data of non leaf node, stores occurrence count
  * And alternatives of children
  *
  * @param children array which stores by index i alternatives for i-th child
  */
class SimInnerNodeData(val children: Array[SNodeChildrenAlternatives]) extends SimNodeData

/**
  * Class for representing alternatives of children
  *
  * @param alternatives map which store info about node 2 link
  */
class SNodeChildrenAlternatives(val alternatives: mutable.Map[SimNodeId, SimNodeData])

object SNodeChildrenAlternatives {
  def apply(): SNodeChildrenAlternatives = new SNodeChildrenAlternatives(new mutable.HashMap[SimNodeId, SimNodeData]())
}

/**
  * Base class for node's id
  * Id should describe node, using in test for equality
  */
sealed abstract class SimNodeId() {
  def elementType: ElementType
}

/**
  * Identifier of node which has children
  *
  * @param elementType   node type
  * @param childrenCount count of children
  */
case class SimInnerNodeId(elementType: ElementType,
                          childrenCount: ChildrenCount) extends SimNodeId

/**
  * Identifier of leaf node
  *
  * @param elementType node type
  * @param nodeText    text of node
  */
case class SimLeafNodeId(elementType: ElementType,
                         nodeText: NodeText) extends SimNodeId

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
