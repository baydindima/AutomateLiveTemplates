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
  * @param commonStatistic common characteristics
  */
class LeafNodeStatistic(val commonStatistic: CommonNodeStatistic) extends NodeStatistic

object LeafNodeStatistic {
  def apply(nodeStatistic: CommonNodeStatistic): LeafNodeStatistic = new LeafNodeStatistic(
    commonStatistic = nodeStatistic
  )
}

class InnerNodeStatistic(val nodeCount: Int,
                         val leafCount: Int,
                         val innerCount: Int,
                         val height: Int,
                         val commonStatistic: CommonNodeStatistic) extends NodeStatistic {
  override def toString: String = super.toString
}

object InnerNodeStatistic {
  def apply(childrenStat: Seq[NodeStatistic],
            commonNodeStatistic: CommonNodeStatistic) = {
    val (leafStat, innerStat): (Seq[LeafNodeStatistic], Seq[InnerNodeStatistic]) =
      ((List.empty[LeafNodeStatistic], List.empty[InnerNodeStatistic]) /: childrenStat) {
        case ((ls, is), s) => s match {
          case stat: InnerNodeStatistic =>
            (ls, stat :: is)
          case stat: LeafNodeStatistic =>
            (stat :: ls, is)
        }
      }
    new InnerNodeStatistic(
      nodeCount = leafStat.size + innerStat.size + innerStat.map(_.nodeCount).sum,
      leafCount = leafStat.size + innerStat.map(_.leafCount).sum,
      innerCount = innerStat.size + innerStat.map(_.innerCount).sum,
      height = innerStat.map(_.height).reduceOption(_ max _).getOrElse(0) + 1,
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