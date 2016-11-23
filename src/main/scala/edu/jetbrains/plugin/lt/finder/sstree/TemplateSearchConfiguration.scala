package edu.jetbrains.plugin.lt.finder.sstree

import edu.jetbrains.plugin.lt.finder.common.Template

/**
  * Contains functions and predicates
  * for building and filtering possible templates
  */
trait TemplateSearchConfiguration {

  def isPossibleTemplateRoot(possibleTemplateRoot: NodeInfo): Boolean

  def isPossibleTemplate(template: Template): Boolean

  def isLikelyChildren(parent: InnerNodeInfo, children: List[NodeInfo]): Boolean

}

object DefaultSearchConfiguration extends TemplateSearchConfiguration {
  val MatchesMinimum = 10
  val LengthMinimum = 36
  val PlaceholderMaximum = 3
  val NodesMinimum = 8
  val PlaceholderToNodeRatio = 0.1

  override def isPossibleTemplateRoot(possibleTemplateRoot: NodeInfo): Boolean =
    possibleTemplateRoot match {
      case node: InnerNodeInfo =>
        node.stat.occurrenceCount >= MatchesMinimum
      case _ => false
    }

  override def isPossibleTemplate(template: Template): Boolean =
    (template.templateStatistic.placeholderCount /
      template.templateStatistic.nodeCount.toDouble) <= PlaceholderToNodeRatio &&
      template.templateStatistic.nodeCount >= NodesMinimum &&
      template.text.length >= LengthMinimum


  override def isLikelyChildren(parent: InnerNodeInfo, children: List[NodeInfo]): Boolean = {
    val parentOccurrenceCount = parent.stat.occurrenceCount
    val childrenOccurrenceCount = children.head.stat.occurrenceCount

    childrenOccurrenceCount >= MatchesMinimum &&
      childrenOccurrenceCount * 10 >= parentOccurrenceCount
  }
}
