package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree.{NodeChildrenAlternatives, NodeId}

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

  override def isPossibleTemplateRoot(templateNode: TemplateNode): Boolean =
    templateNode match {
      case node: TemplateInnerNode =>
        val statistic = node.generalInnerNodeStatistic
        statistic.averageMaxHeight >= MinimumDepth &&
          statistic.commonStatistic.occurrenceCount >= MatchesMinimum
      case _ => false
    }

  override def isPossibleTemplate(root: TemplateNode,
                                  template: Template): Boolean =
    template.templateStatistic.placeholderCount <= PlaceholderMaximum &&
      template.templateStatistic.nodeCount >= NodesMinimum &&
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
