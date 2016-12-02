package edu.jetbrains.plugin.lt.finder.miner


trait FileTypeTemplateFilter {
  def shouldAnalyze(nodeId: NodeId): Boolean
}

object JavaFileTypeTemplateFilter extends FileTypeTemplateFilter {
  def shouldAnalyze(nodeId: NodeId): Boolean = nodeId match {
    case InnerNodeId(elementType) =>
      import com.intellij.psi.impl.source.tree.JavaElementType._
      elementType != IMPORT_STATEMENT && elementType != IMPORT_STATIC_STATEMENT && elementType != PACKAGE_STATEMENT
    case _ => true
  }
}
