package edu.jetbrains.plugin.lt.finder.stree

import scala.collection.mutable

abstract class SNodeLink() {
  private var occurrenceCount: Int = 1

  def getOccurrenceCount: Int = occurrenceCount

  def addOccurrence(): Unit = {
    occurrenceCount += 1
  }

}

class LeafSLink extends SNodeLink

class InnerSLink(val children: Array[SNodeChildrenAlternatives]) extends SNodeLink

class SNodeChildrenAlternatives(val alternatives: mutable.Map[SNodeInfo, SNodeLink])

object SNodeChildrenAlternatives {
  def apply(): SNodeChildrenAlternatives = new SNodeChildrenAlternatives(new mutable.HashMap[SNodeInfo, SNodeLink]())
}

abstract class SNodeInfo() {
  def iElementTypeIndex: IElementTypeIndex
}

case class SInnerNodeInfo(iElementTypeIndex: IElementTypeIndex,
                          childrenCount: ChildrenCount) extends SNodeInfo

case class SLeafNodeInfo(iElementTypeIndex: IElementTypeIndex,
                         nodeText: NodeText) extends SNodeInfo

case class ChildrenCount(value: Int) extends AnyVal

case class NodeText(value: String) extends AnyVal

case class IElementTypeIndex(value: Short) extends AnyVal
