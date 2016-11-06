package edu.jetbrains.plugin.lt

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.fileTypes.{PlainTextFileType, UnknownFileType}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter
import edu.jetbrains.plugin.lt.finder.stree.SimTree
import edu.jetbrains.plugin.lt.finder.template.{TemplateSearcher, TemplateWithFileType}
import edu.jetbrains.plugin.lt.newui.TemplatesDialog

import scala.collection.JavaConversions._

/**
  * Created by Dmitriy Baidin.
  */
class LiveTemplateFindAction extends AnAction {

  override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
    val project = anActionEvent.getProject
    if (project == null) {
      return
    }



    val allFiles = getFiles(
      roots = ProjectRootManager.getInstance(project).getContentSourceRoots,
      psiManager = PsiManager.getInstance(project)
    )
    val filters = FileTypeTemplateFilter.EP_NAME.getExtensions

    //    val totalTemplates: ArrayBuffer[]


    allFiles.groupBy(_.getFileType).foreach { case (fileType, files) =>
      val start = System.currentTimeMillis()

      val astNodes = files.map(_.getNode)

      val tree = new SimTree
      astNodes.foreach(tree.add)
      val templateSearcher = new TemplateSearcher(tree)
      val templates = templateSearcher.searchTemplate

      templates.foreach(x => println(x.text))
      println(s"Time for templates extracting: ${System.currentTimeMillis() - start}")
      new TemplatesDialog(project, templates.map(new TemplateWithFileType(_, fileType))).show()
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
