package edu.jetbrains.plugin.lt

import com.abahgat.suffixtree.GeneralizedSuffixTree
import com.intellij.lang.FileASTNode
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.fileTypes.{FileType, PlainTextFileType, UnknownFileType}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter
import edu.jetbrains.plugin.lt.finder.common.Template
import edu.jetbrains.plugin.lt.finder.miner.{JavaFileTypeNodeFilter, MB3, MinerConfiguration}
import edu.jetbrains.plugin.lt.finder.postprocessor.{DefaultTemplatePostProcessor, DefaultTreeEncodingFormatter}
import edu.jetbrains.plugin.lt.finder.sstree.{DefaultSearchConfiguration, TemplateFilter, TemplateSearchConfiguration}
import edu.jetbrains.plugin.lt.newui.{ChooseFileTypeDialog, ChooseImportantTemplates}
import edu.jetbrains.plugin.lt.ui.NoTemplatesDialog

import scala.annotation.tailrec
import scala.collection.JavaConversions._

/**
  * Created by Dmitriy Baidin.
  */
class LiveTemplateFindAction extends AnAction {

  def removeSubStrings(templates: Seq[Template]): Seq[Template] = {
    val gst = new GeneralizedSuffixTree
    templates.sortBy(-_.text.length).zipWithIndex.filter { case (template, index) =>
      val matches = gst.search(template.text).toSeq
      if (matches.isEmpty) {
        gst.put(template.text, index)
        true
      } else {
        false
      }
    }.map(_._1)
  }

  override def actionPerformed(anActionEvent: AnActionEvent): Unit = {
    val project = anActionEvent.getProject
    if (project == null) {
      return
    }

    val allFiles = getFiles(
      roots = ProjectRootManager.getInstance(project).getContentSourceRoots,
      psiManager = PsiManager.getInstance(project)
    )
//    val filters = FileTypeTemplateFilter.EP_NAME
//      .getExtensions
//      .map(filter => filter.fileType ->
//        (filter.keywordsNotAnalyze ++ filter.keywordsNotShow).toSeq)
//      .toMap

    val fileTypeToFiles = allFiles.groupBy(_.getFileType)

    val chooseFileTypeDialog = new ChooseFileTypeDialog(mapAsJavaMap(fileTypeToFiles.mapValues(_.size)))
    val selectedFileTypes = chooseFileTypeDialog.showDialog().toSet

    @tailrec
    def step(astNodes: Seq[FileASTNode], fileType: FileType, templateSearchConfiguration: TemplateSearchConfiguration): Unit = {
      val templates = getTemplates(astNodes, templateSearchConfiguration)

      if (templates.nonEmpty) {
        val importantTemplates = new ChooseImportantTemplates(project, fileType, templates).showDialog()
        if (importantTemplates.nonEmpty) {
          val newTemplateSearchConfiguration = generateNewTemplateSearchConfiguration(importantTemplates)
          println(s"Generated search config: $newTemplateSearchConfiguration")
          val mergedTemplateSearchConfiguration = templateSearchConfiguration.merge(newTemplateSearchConfiguration)
          println(s"Merged search config: $mergedTemplateSearchConfiguration")
          step(astNodes, fileType, mergedTemplateSearchConfiguration)
        }
      } else {
        new NoTemplatesDialog(project).show()
      }
    }

    if (Option(selectedFileTypes).isDefined) {
      fileTypeToFiles.filter(f => selectedFileTypes(f._1)).foreach { case (fileType, files) =>
        val astNodes = files.map(_.getNode).filter(_ != null)
        if (astNodes.nonEmpty) {
          println(s"File type ${fileType.getName}")
          println(s"AstNodes count: ${astNodes.size}")
          step(astNodes, fileType, DefaultSearchConfiguration)
        }
      }
    }
  }

  private def getTemplates(astNodes: Seq[FileASTNode],
                           templateSearchConfiguration: TemplateSearchConfiguration,
                           desiredTemplateCount: Int = 50,
                           startMinSupportCoefficient: Double = 0.5,
                           stepMinSupportCoefficient: Double = 0.5): Seq[Template] = {
    import LiveTemplateFindAction._
    import Math._

    val templateFilterImpl = new TemplateFilter(templateSearchConfiguration)
    val templateProcessor = new DefaultTemplatePostProcessor {

      override protected val templateFilter: TemplateFilter = templateFilterImpl

      override protected val treeEncodingFormatter = new DefaultTreeEncodingFormatter
    }

    def helper(left: Int, right: Int, bestResult: Seq[Template], minDiff: Int): Seq[Template] = {
      if (left < right) {
        val med = left + (right - left) / 2

        println(s"Free memory ${Runtime.getRuntime.freeMemory()}")
        println(s"Min support coefficient: ${med * MinSupportCoefficientStep}")

        val start = System.currentTimeMillis()

        val templates = new MB3(
          new MinerConfiguration(med * MinSupportCoefficientStep),
          JavaFileTypeNodeFilter).getFrequentTreeEncodings(astNodes)

        val uniqTemplates = templateProcessor.process(templates)

        println(s"Uniq templates count: ${uniqTemplates.size}")

        println(s"Time for templates extracting: ${System.currentTimeMillis() - start}")


        uniqTemplates.size match {
          case _ if uniqTemplates.size == desiredTemplateCount => uniqTemplates
          case _ if uniqTemplates.size < desiredTemplateCount =>
            val curDiff = abs(desiredTemplateCount - uniqTemplates.size)
            if (curDiff < minDiff) {
              helper(left, med, uniqTemplates, curDiff)
            } else {
              helper(left, med, bestResult, minDiff)
            }
          case _ if uniqTemplates.size > desiredTemplateCount =>
            val curDiff = abs(desiredTemplateCount - uniqTemplates.size)

            if (curDiff < minDiff) {
              helper(med + 1, right, uniqTemplates, curDiff)
            } else {
              helper(med + 1, right, bestResult, minDiff)
            }
        }
      } else {
        bestResult
      }
    }

    helper(MinMinSupportCoefficient, MaxMinSupportCoefficient + 1, Seq.empty, Int.MaxValue)
  }

  private def getFiles(roots: Seq[VirtualFile], psiManager: PsiManager): Seq[PsiFile] = {
    def allFilesFrom(psiDirectory: PsiDirectory): Seq[PsiFile] =
      psiDirectory.getFiles ++ psiDirectory.getSubdirectories.flatMap(allFilesFrom)

    roots.map(psiManager.findDirectory)
      .flatMap(allFilesFrom)
      .filter(_.getFileType != PlainTextFileType.INSTANCE)
      .filter(_.getFileType != UnknownFileType.INSTANCE)
  }

  private def generateNewTemplateSearchConfiguration(importantTemplates: Seq[Template]): TemplateSearchConfiguration = {
    val templateStatistics = importantTemplates.map(_.templateStatistic)
    val textLengthes = importantTemplates.map(_.text.length)
    val placeholderMaximum = templateStatistics.map(_.placeholderCount).max

    val nodeLengthes = templateStatistics.map(_.nodeCount)
    new TemplateSearchConfiguration() {
      override val lengthMaximum: Int = textLengthes.max
      override val lengthMinimum: Int = textLengthes.min
      override val maxPlaceholderToNodeRatio: Double = templateStatistics.map(_.placeholderToNodeRatio).max
      override val nodesMaximum: Int = nodeLengthes.max
      override val nodesMinimum: Int = nodeLengthes.min
      override val placeholderMaximum: Int = templateStatistics.map(_.placeholderCount).max
    }
  }
}

object LiveTemplateFindAction {
  val MinMinSupportCoefficient = 1
  val MaxMinSupportCoefficient = 32
  val MinSupportCoefficientStep: Double = 0.5

}


/**
  * Пока первое замечание - лучше переписать API FileTypeTemplateFilter:
  * ввести вместо коллекций функции-чекеры в стиле boolean
  * shouldOmitKeyword(String keyword), это даёт больше свободы в реализации.
  */
/**
  * Для сравнения алгоритмов имеет смысл написать подгонку параметров каким-нибудь методом
  * (хотя бы перебором с шагом для начала), чтобы можно было разделить множество
  * тестов на 2 части, а затем обучить алгоритм (подобрать параметры) на одной,
  * и проверить на другой. Я полагаю, правильно это называется кросс-валидацией.
  */

/**
  * Поскольку у тебя уже есть 3 реализации задачи (алгоритм Егора, твой вариант
  * и алгоритм из статьи) вохможно, имеет смысл в качестве результата практики
  * представить сравнительный анализ алгоритмов. Это выглядит довольно содлидно
  * и очень хорошо смотрися как глава диплома, если ты решишь в качестве диплома
  * довести плагин.
  */

/**
  * Неплохо было бы посмотреть на поиск дубликатов для Java, который уже есть
  * в идее, я полагаю, он живёт в пакете com.intellij.dupLocator.
  * Может, можно что-то оттуда стащить в смысле идей.
  * Ну и, как минимум, на него всегда можно сослаться как на аналог
  * (помимо тех статей о писке дублированного кода, которые мы уже рассматривали в начале семестра).
  */

/**
  * Во-первых, намного лучше, поскольку мы работаем с узлами дерева, множество
  * неинетесных узлов определять также на узлах. Тогда в реализации фильтра для
  * языка можно смэтчить тип узла с известными нам типами узлов (IElementType)
  * для package, import и т.д. Раз уж extension знает, с каким языком он работает,
  * надо использовать это максимально.
  */

/**
  * Во-вторых, возможно, следует следить, чтобы когда в плейсхолдер попадает обычно
  * парный символ (например, различные типы скобок), и при этом где-то есть пара
  * (например, в плейсхолдер попал '[', а где-то далее в тексте есть парная ему ']'),
  * то либо брать оба символа в плейсхолдер, либо не брать ни один из них.
  */