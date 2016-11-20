package edu.jetbrains.plugin.lt

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.fileTypes.{PlainTextFileType, UnknownFileType}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter
import edu.jetbrains.plugin.lt.finder.stree.SimTree
import edu.jetbrains.plugin.lt.finder.template.{DefaultSearchConfiguration, TemplateSearcher, TemplateWithFileType}
import edu.jetbrains.plugin.lt.newui.{TemplatesDialog, TreeStatisticDialog}
import edu.jetbrains.plugin.lt.ui.NoTemplatesDialog

import scala.collection.JavaConversions._

/**
  * Created by Dmitriy Baidin.
  */
class LiveTemplateFindAction extends AnAction {
  val MemorySize = 10

  override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
    val project = anActionEvent.getProject
    if (project == null) {
      return
    }

    val allFiles = getFiles(
      roots = ProjectRootManager.getInstance(project).getContentSourceRoots,
      psiManager = PsiManager.getInstance(project)
    )
    val filters = FileTypeTemplateFilter.EP_NAME
      .getExtensions
      .map(filter => filter.fileType ->
        (filter.keywordsNotAnalyze ++ filter.keywordsNotShow).toSeq)
      .toMap

    allFiles.groupBy(_.getFileType).foreach { case (fileType, files) =>
      val start = System.currentTimeMillis()

      val astNodes = files.map(_.getNode)
      println(s"AstNodes count: ${astNodes.size}")

      val tree = new SimTree
      astNodes.foreach(tree.add)
      val templateSearcher = new TemplateSearcher(tree,
        DefaultSearchConfiguration,
        filters.getOrElse(fileType, Seq.empty)
      )

      val templates = templateSearcher.searchTemplate

      println(s"Time for templates extracting: ${System.currentTimeMillis() - start}")
      new TreeStatisticDialog(project, fileType, tree.calcTreeStatistic).show()
      if (templates.nonEmpty) {
        new TemplatesDialog(project, templates.map(new TemplateWithFileType(_, fileType))).show()
      } else {
        new NoTemplatesDialog(project).show()
      }

    }


  }

  private def getFiles(roots: Seq[VirtualFile], psiManager: PsiManager): Seq[PsiFile] = {
    def allFilesFrom(psiDirectory: PsiDirectory): Seq[PsiFile] =
      psiDirectory.getFiles ++ psiDirectory.getSubdirectories.flatMap(allFilesFrom)

    roots.map(psiManager.findDirectory)
      .flatMap(allFilesFrom)
      .filter(_.getFileType != PlainTextFileType.INSTANCE)
      .filter(_.getFileType != UnknownFileType.INSTANCE)
  }
}
