package edu.jetbrains.plugin.lt

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.fileTypes.{PlainTextFileType, UnknownFileType}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter
import edu.jetbrains.plugin.lt.finder.common.TemplateWithFileType
import edu.jetbrains.plugin.lt.finder.miner.{LeafNodeId, MB3}
import edu.jetbrains.plugin.lt.finder.sstree.DefaultSearchConfiguration
import edu.jetbrains.plugin.lt.newui.TemplatesDialog
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

    allFiles.filter(_.getFileType == JavaFileType.INSTANCE).groupBy(_.getFileType).foreach { case (fileType, files) =>
      val astNodes = files.map(_.getNode).filter(_ != null)
      if (astNodes.nonEmpty) {
        println(s"File type ${fileType.getName}")
        println(s"AstNodes count: ${astNodes.size}")
        println(s"Free memory ${Runtime.getRuntime.freeMemory()}")

        val start = System.currentTimeMillis()

        val mb3 = new MB3

        val nodeOccurrence = mb3.getNodeOccurrence(astNodes)

        val nodeCount = nodeOccurrence.size


        val nodeOccurrenceCount = nodeOccurrence.values.sum
        val minSupport = (nodeOccurrenceCount / nodeCount) / 2

        println(s"Node count: $nodeCount")
        println(s"Node occurrence count: $nodeOccurrenceCount")
        println(s"Min support: $minSupport")

        val freqNodes = nodeOccurrence.filter(_._2 >= minSupport).keys.toSet
        println(s"Freq node count: ${freqNodes.size}")
        println(s"Freq node occurrence count: ${nodeOccurrence.filter { case (n, c) => freqNodes(n) }.values.sum}")

        val leaves = freqNodes.filter {
          case l: LeafNodeId => true
          case _ => false
        }

        println(s"Leaves count: ${leaves.size}")
        println(s"Leaves occurrence count: ${nodeOccurrence.filter { case (n, c) => leaves(n) }.values.sum}")

        val dict = mb3.buildDictionary(astNodes, freqNodes)
        println(s"Dictionary length ${dict.length}")

        mb3.start(minSupport, dict)

        mb3.treeMap.map(_.getTemplate).foreach {
          str =>
            println(str.text)
            println(if (DefaultSearchConfiguration.isPossibleTemplate(str)) "valid" else 'invalid)
            println("_______________________")
        }


        val templates = mb3.treeMap.map(_.getTemplate).filter(DefaultSearchConfiguration.isPossibleTemplate)
        println(s"Tree count: ${mb3.treeMap.size}")

        //        val edgeOccurrence = mb3.getEdgeOccurrence(astNodes, freqNodes)
        //        val freqEdges = edgeOccurrence.filter(_._2 >= minSupport)
        //        println(s"Edge count: ${edgeOccurrence.size}")
        //        println(s"Edge occurrence count: ${edgeOccurrence.values.sum}")
        //        println(s"Freq edge count: ${freqEdges.size}")
        //        println(s"Freq edge count: ${freqEdges.values.sum}")

        //        val tree = new SimTree
        //        astNodes.foreach(node => {
        //          tree.add(node)
        //        })
        //
        //
        //
        //        val templateSearcher = new TemplateSearcher(tree,
        //          DefaultSearchConfiguration,
        //          filters.getOrElse(fileType, Seq.empty)
        //        )
        //
        //        val templates = templateSearcher.searchTemplate

        println(s"Time for templates extracting: ${System.currentTimeMillis() - start}")
        //        new TreeStatisticDialog(project, fileType, tree.calcTreeStatistic).show()
        //        new TemplateRootsDialog(project, templateSearcher.possibleTemplateRoot.toSeq).show()
        if (templates.nonEmpty) {
          new TemplatesDialog(project, templates.map(new TemplateWithFileType(_, fileType))).show()
        } else {
          new NoTemplatesDialog(project).show()
        }
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
