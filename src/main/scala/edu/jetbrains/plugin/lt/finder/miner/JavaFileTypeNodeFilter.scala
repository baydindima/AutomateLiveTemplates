package edu.jetbrains.plugin.lt.finder.miner

import edu.jetbrains.plugin.lt.finder.common.Template


trait FileTypeNodeFilter {
  def shouldAnalyze(nodeId: NodeId): Boolean
}

class TemplateContext(val parenthesisStack: List[String]) {
  def process(template: Template): Template = template
}

object JavaFileTypeNodeFilter extends FileTypeNodeFilter {

  import com.intellij.psi.impl.source.tree.JavaElementType._
  import com.intellij.psi.impl.source.tree.JavaDocElementType._

  def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      elementType != IMPORT_STATEMENT && elementType != IMPORT_STATIC_STATEMENT && elementType != PACKAGE_STATEMENT && elementType != DOC_COMMENT
    case _ => true
  }

}
