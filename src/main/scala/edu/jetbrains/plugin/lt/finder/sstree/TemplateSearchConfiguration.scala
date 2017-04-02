package edu.jetbrains.plugin.lt.finder.sstree

import edu.jetbrains.plugin.lt.finder.common.Template
import kotlin.reflect.jvm.internal.impl.resolve.constants.DoubleValue

/**
  * Contains functions and predicates
  * for building and filtering possible templates
  */
trait TemplateSearchConfiguration {
  def lengthMinimum: Int
  def lengthMaximum: Int
  def placeholderMaximum: Int
  def nodesMinimum: Int
  def nodesMaximum: Int
  def placeholderToNodeRatio: Double
}

object DefaultSearchConfiguration extends TemplateSearchConfiguration {
  val lengthMinimum = 30
  val lengthMaximum = 300
  val placeholderMaximum = 3
  val nodesMinimum = 3
  val nodesMaximum = 300
  val placeholderToNodeRatio = 0.5
}

class TemplateFilter(templateSearchConfiguration: TemplateSearchConfiguration) {
  import templateSearchConfiguration._

  def isPossibleTemplate(template: Template): Boolean =
    template.templateStatistic.placeholderCount <= placeholderMaximum &&
      (template.templateStatistic.placeholderCount /
        template.templateStatistic.nodeCount.toDouble) <= placeholderToNodeRatio &&
      template.templateStatistic.nodeCount >= nodesMinimum &&
      template.templateStatistic.nodeCount <= nodesMaximum &&
      template.text.length >= lengthMinimum &&
      template.text.length <= lengthMaximum
}

