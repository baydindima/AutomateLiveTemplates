package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.common.{InnerNodeId, LeafNodeId, NodeId}

/**
  * Class for aggregating id and data
  */
sealed abstract class SimNode {
  def nodeId: NodeId

  def data: SimNodeData
}

object SimNode {
  def apply(nodeId: NodeId,
            data: SimNodeData): SimNode = (nodeId, data) match {
    case (id: LeafNodeId, d: SimLeafNodeData) =>
      new SimLeafNode(id, d)
    case (id: InnerNodeId, d: SimInnerNodeData) =>
      new SimInnerNode(id, d)
    case _ =>
      throw new RuntimeException(s"Id and data has incompatible types: $nodeId and $data")
  }
}

/**
  * Class for description of leaves
  */
class SimLeafNode(val nodeId: LeafNodeId,
                  val data: SimLeafNodeData) extends SimNode

object SimLeafNode {
  def unapply(arg: SimLeafNode): Option[(LeafNodeId, SimLeafNodeData)] = Some(arg.nodeId, arg.data)
}

/**
  * Class for description of inner nodes
  */
class SimInnerNode(val nodeId: InnerNodeId,
                   val data: SimInnerNodeData) extends SimNode

object SimInnerNode {
  def unapply(arg: SimInnerNode): Option[(InnerNodeId, SimInnerNodeData)] = Some(arg.nodeId, arg.data)
}

/**
  * Base class for holding all data of node with certain nodeId
  */
sealed abstract class SimNodeData() {
  /**
    * Count of occurrence of node with certain nodeId in all AST-Trees
    */
  private var occurrenceCount: Int = 0
  /**
    * Count of different parents of node with certain nodeId in all AST-Trees
    */
  private var differentParentCount: Int = 0

  def getOccurrenceCount: Int = occurrenceCount

  def getDifferentParentCount: Int = differentParentCount

  def addOccurrence(): Unit =
    occurrenceCount += 1

  def addDifferentParentCount(): Unit =
    differentParentCount += 1
}

object SimNodeData {
  def apply(childrenCount: Int): SimNodeData = childrenCount match {
    case 0 =>
      SimLeafNodeData.empty
    case n =>
      SimInnerNodeData.empty(childrenCount)
  }
}

class SimLeafNodeData(val statistic: SimLeafNodeStatistic) extends SimNodeData

object SimLeafNodeData {
  def empty: SimLeafNodeData = new SimLeafNodeData(SimLeafNodeStatistic.empty)
}


class SimInnerNodeData(val children: Array[NodeChildrenAlternatives],
                       val statistic: SimInnerNodeStatistic) extends SimNodeData

object SimInnerNodeData {
  def empty(childrenCount: Int): SimInnerNodeData =
    new SimInnerNodeData(
      Array.fill[NodeChildrenAlternatives](childrenCount) {
        NodeChildrenAlternatives()
      },
      SimInnerNodeStatistic.empty
    )
}

