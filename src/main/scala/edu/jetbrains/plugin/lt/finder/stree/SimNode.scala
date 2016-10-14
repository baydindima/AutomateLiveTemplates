package edu.jetbrains.plugin.lt.finder.stree

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
class SimInnerNodeData(val children: Array[NodeChildrenAlternatives[SimNode]]) extends SimNodeData