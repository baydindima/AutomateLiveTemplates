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

  def apply(parentCommonStatistic: CommonNodeStatistic, siblingsCount: Int) = new CommonNodeStatistic(
    depth = parentCommonStatistic.depth + 1,
    siblingsCount = siblingsCount
  )
}

/**
  * Node characteristics for leaves
  *
  * @param textLength      length of text in node
  * @param commonStatistic common characteristics
  */
class LeafNodeStatistic(val textLength: Int,
                        val commonStatistic: CommonNodeStatistic) extends NodeStatistic

object LeafNodeStatistic {
  def apply(nodeStatistic: CommonNodeStatistic,
            node: SimLeafNode): LeafNodeStatistic = new LeafNodeStatistic(
    textLength = node.nodeId.nodeText.value.length,
    commonStatistic = nodeStatistic
  )
}

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

object InnerNodeStatistic {
  def apply(childrenStat: Seq[NodeStatistic],
            commonNodeStatistic: CommonNodeStatistic) = {
    val (leafStat, innerStat): (Seq[LeafNodeStatistic], Seq[InnerNodeStatistic]) =
      ((List.empty[LeafNodeStatistic], List.empty[InnerNodeStatistic]) /: childrenStat) {
        case ((ls, is), s) ⇒ s match {
          case stat: InnerNodeStatistic ⇒
            (ls, stat :: is)
          case stat: LeafNodeStatistic ⇒
            (stat :: ls, is)
        }
      }
    new InnerNodeStatistic(
      nodeCount = leafStat.size + innerStat.size + innerStat.map(_.nodeCount).sum,
      leafCount = leafStat.size + innerStat.map(_.leafCount).sum,
      innerCount = innerStat.size + innerStat.map(_.innerCount).sum,
      maxDegreeSubtree = childrenStat.size max innerStat.map(_.maxDegreeSubtree).reduceOption(_ max _).getOrElse(0),
      maxHeight = innerStat.map(_.maxHeight).reduceOption(_ max _).getOrElse(0) + 1,
      minHeight = leafStat.headOption.map(_ ⇒ 0).getOrElse(innerStat.map(_.minHeight).min) + 1,
      averageHeight = (leafStat.size + innerStat.map(_.averageHeight + 1).sum) / childrenStat.size.toDouble,
      commonStatistic = commonNodeStatistic
    )
  }
}

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