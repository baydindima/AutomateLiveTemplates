package edu.jetbrains.plugin.lt.finder.stree

import scala.collection.mutable.ArrayBuffer

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
    case (id: LeafNodeId, d: SimLeafNodeData) ⇒
      new SimLeafNode(id, d)
    case (id: InnerNodeId, d: SimInnerNodeData) ⇒
      new SimInnerNode(id, d)
    case _ ⇒
      throw new RuntimeException(s"Id and data has incompatible types: $nodeId and $data")
  }
}

class SimLeafNode(val nodeId: LeafNodeId,
                  val data: SimLeafNodeData) extends SimNode

class SimInnerNode(val nodeId: InnerNodeId,
                   val data: SimInnerNodeData) extends SimNode

/**
  * Base class for links, also stores occurrence of node
  */
sealed abstract class SimNodeData() {
  private var occurrenceCount: Int = 1
  private var differentParentCount: Int = 0

  def getOccurrenceCount: Int = occurrenceCount

  def getDifferentParentCount: Int = differentParentCount

  def addOccurrence(): Unit =
    occurrenceCount += 1

  def addDifferentParentCount(): Unit =
    differentParentCount += 1
}

/**
  * Data to children of leaf node, stores only occurrence count
  */
class SimLeafNodeData(val statistics: ArrayBuffer[LeafNodeStatistic]) extends SimNodeData

object SimLeafNodeData {
  def empty: SimLeafNodeData = new SimLeafNodeData(new ArrayBuffer[LeafNodeStatistic]())
}

/**
  * Data of non leaf node, stores occurrence count
  * And alternatives of children
  *
  * @param children array which stores by index i alternatives for i-th child
  */
class SimInnerNodeData(val children: Array[NodeChildrenAlternatives[SimNode]],
                       val statistics: ArrayBuffer[InnerNodeStatistic]) extends SimNodeData

object SimInnerNodeData {
  def empty(childrenCount: Int): SimInnerNodeData =
    new SimInnerNodeData(
      Array.fill[NodeChildrenAlternatives[SimNode]](childrenCount) {
        NodeChildrenAlternatives()
      },
      new ArrayBuffer[InnerNodeStatistic]()
    )
}

trait NodeStatistic

class CommonNodeStatistic(val depth: Int,
                          val siblingsCount: Int)

object CommonNodeStatistic {
  def empty: CommonNodeStatistic = new CommonNodeStatistic(depth = 0, siblingsCount = 0)
}

class LeafNodeStatistic(val textLength: Int,
                        val commonStatistic: CommonNodeStatistic) extends NodeStatistic

class InnerNodeStatistic(val nodeCount: Int,
                         val leafCount: Int,
                         val innerCount: Int,
                         val maxDegreeSubtree: Int,
                         val maxHeight: Int,
                         val minHeight: Int,
                         val averageHeight: Double,
                         val commonStatistic: CommonNodeStatistic) extends NodeStatistic


/**
  * list in [[edu.jetbrains.plugin.lt.finder.stree.SimNodeData]]
  * STATISTICS FOR NODE IN AST-TREE
  * Depth
  * Siblings count ?
  *
  * Only for inner nodes:
  * Subtree size
  * Leaf count
  * Inner count
  * Max degree in subtree
  * Max height
  * Min height
  * Average height
  *
  * Only for leaf nodes:
  * Text length
  *
  */

/**
  * field in [[edu.jetbrains.plugin.lt.finder.stree.SimNodeData]]
  * STATISTICS FOR NODE IN S-AST-TREE
  * Occurrence count
  * Count of different parent ?
  *
  * For inner node
  * Child - alternative count
  * Alternative - occurrence count
  *
  */


/**
  * field in [[edu.jetbrains.plugin.lt.finder.stree.SimTree]]
  * TOTAL STATISTICS OF S-TREE
  * Count of trees
  * Count of different roots
  * Count of nodes / count of occurrence
  * Count of leafs / count of occurrence of leafs
  * Count of inner nodes / count of occurrence of inner nodes
  * Max count of alternatives
  * Average count of alternatives
  * Max frequency count
  */
