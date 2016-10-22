package edu.jetbrains.plugin.lt.finder.stree


/**
  * Node statistic in AST-Tree
  */
trait NodeStatistic

/**
  * Node characteristics for all type of node
  *
  * @param depth         depth in ast tree
  * @param siblingsCount count of sibling node
  */
class CommonNodeStatistic(val depth: Int,
                          val siblingsCount: Int)

object CommonNodeStatistic {
  def empty: CommonNodeStatistic = new CommonNodeStatistic(depth = 0, siblingsCount = 0)
}

/**
  * Node characteristics for leaves
  *
  * @param textLength      length of text in node
  * @param commonStatistic common characteristics
  */
class LeafNodeStatistic(val textLength: Int,
                        val commonStatistic: CommonNodeStatistic) extends NodeStatistic

/**
  * Node characteristics for inner nodes
  *
  * @param nodeCount        count of nodes in subtree
  * @param leafCount        count of leaves in subtree
  * @param innerCount       count of inner nodes in subtree
  * @param maxDegreeSubtree max degree of node in subtree
  * @param maxHeight        max height
  * @param minHeight        min height
  * @param averageHeight    sum of height of children divided by count of children
  * @param commonStatistic  common characteristics
  */
class InnerNodeStatistic(val nodeCount: Int,
                         val leafCount: Int,
                         val innerCount: Int,
                         val maxDegreeSubtree: Int,
                         val maxHeight: Int,
                         val minHeight: Int,
                         val averageHeight: Double,
                         val commonStatistic: CommonNodeStatistic) extends NodeStatistic

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