package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree._

/**
  * Created by Dmitriy Baidin.
  */
sealed abstract class TemplateNode


class TemplateLeafNode(val nodeId: LeafNodeId,
                       val generalLeafStatistic: GeneralLeafNodeStatistic) extends TemplateNode

class TemplateInnerNode(val nodeId: InnerNodeId,
                        val children: Array[TemplateNode],
                        val generalInnerNodeStatistic: GeneralInnerNodeStatistic) extends TemplateNode

object TemplatePlaceholder extends TemplateNode

class GeneralCommonStatistic(val occurrenceCount: Int,
                             val differentParentCount: Int,
                             val averageDepth: Double,
                             val averageSiblingsCount: Double)

object GeneralCommonStatistic {
  def apply(occurrenceCount: Int,
            differentParentCount: Int,
            commonStatistics: Seq[CommonNodeStatistic]): GeneralCommonStatistic = {
    val statCount = commonStatistics.size
    val (sumDepth, sumSiblingsCount) = ((0, 0) /: commonStatistics) {
      case ((sd, ss), stat) ⇒
        (sd + stat.depth, ss + stat.siblingsCount)
    }
    new GeneralCommonStatistic(
      occurrenceCount = occurrenceCount,
      differentParentCount = differentParentCount,
      averageDepth = sumDepth.toDouble / statCount,
      averageSiblingsCount = sumSiblingsCount.toDouble / statCount
    )
  }

}

class GeneralLeafNodeStatistic(val textLength: Int,
                               commonStatistic: GeneralCommonStatistic)

object GeneralLeafNodeStatistic {
  def apply(occurrenceCount: Int,
            differentParentCount: Int,
            leafNodeStatistics: Seq[LeafNodeStatistic]): GeneralLeafNodeStatistic = {
    val commonStatistic = GeneralCommonStatistic(
      occurrenceCount,
      differentParentCount,
      leafNodeStatistics.map(_.commonStatistic)
    )
    val textLength = leafNodeStatistics.head.textLength
    new GeneralLeafNodeStatistic(
      textLength = textLength,
      commonStatistic = commonStatistic
    )
  }
}

class GeneralInnerNodeStatistic(val childrenCount: Int,
                                val averageNodeCount: Double,
                                val averageLeafCount: Double,
                                val averageInnerCount: Double,
                                val averageMaxDegreeSubtree: Double,
                                val averageMaxHeight: Double,
                                val averageMinHeight: Double,
                                val generalAverageHeight: Double,
                                val commonStatistic: GeneralCommonStatistic)

object GeneralInnerNodeStatistic {
  def apply(childrenCount: Int,
            occurrenceCount: Int,
            differentParentCount: Int,
            innerNodeStatistics: Seq[InnerNodeStatistic]): GeneralInnerNodeStatistic = {
    val statCount = innerNodeStatistics.size
    val commonStatistic = GeneralCommonStatistic(
      occurrenceCount = occurrenceCount,
      differentParentCount = differentParentCount,
      commonStatistics = innerNodeStatistics.map(_.commonStatistic)
    )
    val (sumNodeCount,
    sumLeafCount,
    sumInnerCount,
    sumMaxDegreeSubtree,
    sumMaxHeight,
    sumMinHeight,
    sumAverageHeight) = ((0, 0, 0, 0, 0, 0, 0.0) /: innerNodeStatistics) {
      case ((sn, sl, si, sd, sMaxH, sMinH, sa), stat) ⇒
        (sn + stat.nodeCount,
          sl + stat.leafCount,
          si + stat.innerCount,
          sd + stat.maxDegreeSubtree,
          sMaxH + stat.maxHeight,
          sMinH + stat.minHeight,
          sa + stat.averageHeight)
    }
    new GeneralInnerNodeStatistic(
      childrenCount = childrenCount,
      averageNodeCount = sumNodeCount.toDouble / statCount,
      averageLeafCount = sumLeafCount.toDouble / statCount,
      averageInnerCount = sumInnerCount.toDouble / statCount,
      averageMaxDegreeSubtree = sumMaxDegreeSubtree.toDouble / statCount,
      averageMaxHeight = sumMaxHeight.toDouble / statCount,
      averageMinHeight = sumMinHeight.toDouble / statCount,
      generalAverageHeight = sumAverageHeight / statCount,
      commonStatistic = commonStatistic
    )
  }
}

/**
  * Should contain general statistic of node
  * maybe several variant of children
  */