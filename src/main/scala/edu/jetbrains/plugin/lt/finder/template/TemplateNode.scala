package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree._

/**
  * Created by Dmitriy Baidin.
  */
sealed abstract class TemplateNode {
  def getStatisticsString: String
}


class TemplateLeafNode(val nodeId: LeafNodeId,
                       val generalLeafStatistic: GeneralLeafNodeStatistic) extends TemplateNode {
  override def toString: String = s"Leaf node: ${nodeId.nodeText}"

  override def getStatisticsString: String = generalLeafStatistic.toString
}

class TemplateInnerNode(val nodeId: InnerNodeId,
                        val children: Array[TemplateNode],
                        val generalInnerNodeStatistic: GeneralInnerNodeStatistic) extends TemplateNode {
  override def toString: String =
    s"Inner node: ${nodeId.elementType.toString} with children count ${nodeId.childrenCount}"

  override def getStatisticsString: String =
    generalInnerNodeStatistic.toString
}

object TemplatePlaceholder extends TemplateNode {
  override def toString: String = "PLACEHOLDER"

  override def getStatisticsString: String = "PLACEHOLDER"
}

class GeneralCommonStatistic(val occurrenceCount: Int,
                             val differentParentCount: Int,
                             val commonStatistic: SimCommonNodeStatistic) {
  override def toString: String =
    s"""Occurrence count: $occurrenceCount
        |Different parent count: $differentParentCount
        |$commonStatistic
    """.stripMargin
}

object GeneralCommonStatistic {
  def apply(occurrenceCount: Int,
            differentParentCount: Int,
            commonStatistic: SimCommonNodeStatistic): GeneralCommonStatistic = {
    new GeneralCommonStatistic(
      occurrenceCount = occurrenceCount,
      differentParentCount = differentParentCount,
      commonStatistic = commonStatistic
    )
  }

}

class GeneralLeafNodeStatistic(val textLength: Int,
                               commonStatistic: GeneralCommonStatistic) {
  override def toString: String =
    s"""text length: $textLength
        | $commonStatistic
    """.stripMargin
}

object GeneralLeafNodeStatistic {
  def apply(occurrenceCount: Int,
            differentParentCount: Int,
            leafNodeId: LeafNodeId,
            leafNodeStatistic: SimLeafNodeStatistic): GeneralLeafNodeStatistic = {
    new GeneralLeafNodeStatistic(
      textLength = leafNodeId.nodeText.length,
      commonStatistic = GeneralCommonStatistic(occurrenceCount,
        differentParentCount,
        leafNodeStatistic.commonStatistic
      )
    )
  }
}

class GeneralInnerNodeStatistic(val childrenCount: Int,
                                val nodeStat: SimInnerNodeStatistic,
                                val commonStatistic: GeneralCommonStatistic) {
  override def toString: String =
    s""" Children count: $childrenCount
        | $nodeStat
        | $commonStatistic
    """.stripMargin
}

object GeneralInnerNodeStatistic {
  def apply(childrenCount: Int,
            occurrenceCount: Int,
            differentParentCount: Int,
            innerNodeStatistic: SimInnerNodeStatistic): GeneralInnerNodeStatistic = {
    new GeneralInnerNodeStatistic(
      childrenCount = childrenCount,
      nodeStat = innerNodeStatistic,
      commonStatistic = GeneralCommonStatistic(
        occurrenceCount,
        differentParentCount,
        innerNodeStatistic.commonStatistic
      )
    )
  }
}

/**
  * Should contain general statistic of node
  * maybe several variant of children
  */