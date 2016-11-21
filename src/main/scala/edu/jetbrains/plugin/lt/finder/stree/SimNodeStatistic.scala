package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.StatisticIntParameter

trait SimNodeStatistic

class SimCommonNodeStatistic(val depth: StatisticIntParameter,
                             val siblingsCount: StatisticIntParameter) {
  def +=(stat: CommonNodeStatistic): Unit = {
    depth += stat.depth
    siblingsCount += stat.siblingsCount
  }

  override def toString: String =
    s"""Common node statistic:
        | Depth: $depth
        | Siblings count: $siblingsCount
    """.stripMargin
}

object SimCommonNodeStatistic {
  def empty: SimCommonNodeStatistic = new SimCommonNodeStatistic(
    new StatisticIntParameter,
    new StatisticIntParameter
  )
}


class SimInnerNodeStatistic(val nodeCount: StatisticIntParameter,
                            val leafCount: StatisticIntParameter,
                            val innerCount: StatisticIntParameter,
                            val height: StatisticIntParameter,
                            val commonStatistic: SimCommonNodeStatistic) extends SimNodeStatistic {
  def +=(stat: InnerNodeStatistic): Unit = {
    nodeCount += stat.nodeCount
    leafCount += stat.leafCount
    innerCount += stat.innerCount
    height += stat.height
    commonStatistic += stat.commonStatistic
  }

  override def toString: String =
    s"""Inner node statistic:
        |Node count: $nodeCount
        |Leaf node count: $leafCount
        |Inner node count: $innerCount
        |Height: $height
        |$commonStatistic
    """.stripMargin
}

object SimInnerNodeStatistic {
  def empty: SimInnerNodeStatistic =
    new SimInnerNodeStatistic(
      new StatisticIntParameter,
      new StatisticIntParameter,
      new StatisticIntParameter,
      new StatisticIntParameter,
      SimCommonNodeStatistic.empty
    )
}

class SimLeafNodeStatistic(val commonStatistic: SimCommonNodeStatistic) extends SimNodeStatistic {
  def +=(stat: LeafNodeStatistic): Unit = commonStatistic += stat.commonStatistic

  override def toString: String = commonStatistic.toString
}

object SimLeafNodeStatistic {
  def empty: SimLeafNodeStatistic =
    new SimLeafNodeStatistic(SimCommonNodeStatistic.empty)
}