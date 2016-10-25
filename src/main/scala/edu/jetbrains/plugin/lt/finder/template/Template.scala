package edu.jetbrains.plugin.lt.finder.template

/**
  * Created by Dmitriy Baidin.
  */
class Template(val text: String,
               val templateStatistic: TemplateStatistic)

object Template {
  def apply(templates: Seq[Template]): Template =
    new Template(templates.map(_.text).mkString, TemplateStatistic(templates.map(_.templateStatistic)))
}

class TemplateStatistic(val placeholderCount: Int)

object TemplateStatistic {
  def placeholder: TemplateStatistic = new TemplateStatistic(1)

  def apply(statistics: Seq[TemplateStatistic]): TemplateStatistic =
    new TemplateStatistic(statistics.map(_.placeholderCount).sum)
}


object PlaceholderTemplate extends Template("###", TemplateStatistic.placeholder) {

}