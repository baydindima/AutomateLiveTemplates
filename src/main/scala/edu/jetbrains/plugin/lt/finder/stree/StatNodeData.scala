package edu.jetbrains.plugin.lt.finder.stree


/**
  * Class for aggregating id and data for statistic node
  */
sealed abstract class StatNode {
  def nodeId: NodeId

  def data: StatNodeData
}

/**
  * Class for aggregating id and data for statistic leaf node
  */
class StatLeafNode(val nodeId: LeafNodeId,
                   val data: StatLeafNodeData) extends StatNode

/**
  * Class for aggregating id and data for statistic inner node
  */
class StatInnerNode(val nodeId: InnerNodeId,
                    val data: StatInnerNodeData) extends StatNode

/**
  * Base class for links, also stores occurrence of node
  */
sealed abstract class StatNodeData() {
  def statData: StatData
}

/**
  * Data to children of leaf node, stores only occurrence count
  */
class StatLeafNodeData(val statData: StatData) extends StatNodeData

/**
  * Data of non leaf node, stores occurrence count
  * And alternatives of children
  *
  * @param children array which stores by index i alternatives for i-th child
  */
class StatInnerNodeData(val statData: StatData,
                        val children: Array[NodeChildrenAlternatives[StatNode]]) extends StatNodeData

case class StatData(maxHeight: Int,
                    depth: Int)