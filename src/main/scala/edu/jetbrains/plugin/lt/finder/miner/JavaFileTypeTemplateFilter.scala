package edu.jetbrains.plugin.lt.finder.miner

import edu.jetbrains.plugin.lt.finder.common.Template


trait FileTypeTemplateFilter {
  def shouldAnalyze(nodeId: NodeId): Boolean
}

class TemplateContext(val parenthesisStack: List[String]) {
  def process(template: Template): Template = template
}

object JavaFileTypeTemplateFilter extends FileTypeTemplateFilter {

  import com.intellij.psi.impl.source.tree.JavaElementType._

  def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      elementType != IMPORT_STATEMENT && elementType != IMPORT_STATIC_STATEMENT && elementType != PACKAGE_STATEMENT
    case _ => true
  }

}
