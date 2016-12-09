package edu.jetbrains.plugin.lt.finder.common

import com.intellij.openapi.fileTypes.FileType

/**
  * Created by Dmitriy Baidin.
  */
class TemplateWithFileType(val template: Template,
                           val fileType: FileType)

case class Template(text: String,
                    templateStatistic: TemplateStatistic)

object Template {
  def apply(templates: Seq[Template]): Template =
    new Template(templates.map(_.text).mkString(" "), TemplateStatistic(templates.map(_.templateStatistic)))
}

class TemplateStatistic(val placeholderCount: Int,
                        val nodeCount: Int)

object TemplateStatistic {
  def placeholder: TemplateStatistic = new TemplateStatistic(1, 1)

  def apply(statistics: Seq[TemplateStatistic]): TemplateStatistic =
    new TemplateStatistic(statistics.map(_.placeholderCount).sum,
      statistics.map(_.nodeCount).sum + 1
    )
}


object PlaceholderTemplate extends Template("###", TemplateStatistic.placeholder)