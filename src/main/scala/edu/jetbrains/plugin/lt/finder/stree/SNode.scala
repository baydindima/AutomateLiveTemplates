package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.psi.tree.IElementType

import scala.collection.mutable

/**
  * Base class for links, also stores occurrence of node
  */
sealed abstract class SNodeData() {
  private var occurrenceCount: Int = 1

  def getOccurrenceCount: Int = occurrenceCount

  def addOccurrence(): Unit = {
    occurrenceCount += 1
  }

}

/**
  * Data to children of leaf node, stores only occurrence count
  */
class SLeafNodeData extends SNodeData

/**
  * Data of non leaf node, stores occurrence count
  * And alternatives of children
  *
  * @param children array which stores by index i alternatives for i-th child
  */
class SInnerNodeData(val children: Array[SNodeChildrenAlternatives]) extends SNodeData

/**
  * Class for representing alternatives of children
  *
  * @param alternatives map which store info about node 2 link
  */
class SNodeChildrenAlternatives(val alternatives: mutable.Map[SNodeId, SNodeData])

object SNodeChildrenAlternatives {
  def apply(): SNodeChildrenAlternatives = new SNodeChildrenAlternatives(new mutable.HashMap[SNodeId, SNodeData]())
}

/**
  * Base class for node's id
  * Id should describe node, using in test for equality
  */
sealed abstract class SNodeId() {
  def iElementTypeIndex: ElementType
}

/**
  * Identifier of node which has children
  *
  * @param iElementTypeIndex node type
  * @param childrenCount     count of children
  */
case class SInnerNodeId(iElementTypeIndex: ElementType,
                        childrenCount: ChildrenCount) extends SNodeId

/**
  * Identifier of leaf node
  *
  * @param iElementTypeIndex node type
  * @param nodeText          text of node
  */
case class SLeafNodeId(iElementTypeIndex: ElementType,
                       nodeText: NodeText) extends SNodeId

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
