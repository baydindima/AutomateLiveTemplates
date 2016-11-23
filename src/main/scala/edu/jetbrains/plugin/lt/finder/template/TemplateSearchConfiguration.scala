package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.common.{NodeId, Template}
import edu.jetbrains.plugin.lt.finder.stree.NodeChildrenAlternatives

/**
  * Contains functions and predicates
  * for building and filtering possible templates
  */
trait TemplateSearchConfiguration {

  def isPossibleTemplateRoot(templateNode: TemplateNode): Boolean

  def isPossibleTemplate(root: TemplateNode, template: Template): Boolean

  def getMostLikelyChild(alternative: NodeChildrenAlternatives): Option[NodeId]

}

object DefaultSearchConfiguration extends TemplateSearchConfiguration {
  val MatchesMinimum = 10
  val LengthMinimum = 36
  val PlaceholderMaximum = 3
  val NodesMinimum = 8
  val MinimumDepth = 5
  val PlaceholderToNodeRatio = 0.1

  override def isPossibleTemplateRoot(templateNode: TemplateNode): Boolean =
    templateNode match {
      case node: TemplateInnerNode =>
        //        val statistic = node.generalInnerNodeStatistic
        //        statistic.nodeStat.height.getAverage >= MinimumDepth &&
        //          statistic.commonStatistic.occurrenceCount >= MatchesMinimum
        val statistic = node.generalInnerNodeStatistic
        statistic.commonStatistic.occurrenceCount >= MatchesMinimum
      case _ => false
    }

  override def isPossibleTemplate(root: TemplateNode,
                                  template: Template): Boolean =
    template.templateStatistic.placeholderCount /
      template.templateStatistic.nodeCount.toDouble <= PlaceholderToNodeRatio &&
      template.text.length >= LengthMinimum


  override def getMostLikelyChild(alternative: NodeChildrenAlternatives): Option[NodeId] = {
    val totalCount = alternative.alternativeFrequency.values.sum
    val max = alternative.alternativeFrequency.maxBy(_._2)

    if (totalCount * 0.15 <= max._2 && max._2 >= MatchesMinimum) {
      Some(max._1)
    } else {
      None
    }
  }
}
