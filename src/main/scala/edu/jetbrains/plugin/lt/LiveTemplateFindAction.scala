package edu.jetbrains.plugin.lt

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.fileTypes.{PlainTextFileType, UnknownFileType}
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.{PsiDirectory, PsiFile, PsiManager}
import edu.jetbrains.plugin.lt.extensions.ep.FileTypeTemplateFilter
import edu.jetbrains.plugin.lt.finder.common.TemplateWithFileType
import edu.jetbrains.plugin.lt.finder.miner.{JavaFileTypeTemplateFilter, JavaTemplateProcessor, MB3, MinerConfiguration}
import edu.jetbrains.plugin.lt.finder.sstree.DefaultSearchConfiguration
import edu.jetbrains.plugin.lt.newui.{ChooseFileTypeDialog, ChooseImportantTemplates, TemplatesDialog}
import edu.jetbrains.plugin.lt.ui.NoTemplatesDialog

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
    val filters = FileTypeTemplateFilter.EP_NAME
      .getExtensions
      .map(filter => filter.fileType ->
        (filter.keywordsNotAnalyze ++ filter.keywordsNotShow).toSeq)
      .toMap

    val fileTypeToFiles = allFiles.groupBy(_.getFileType)

    val chooseFileTypeDialog = new ChooseFileTypeDialog(mapAsJavaMap(fileTypeToFiles.mapValues(_.size)))
    val selectedFileTypes = chooseFileTypeDialog.showDialog().toSet

    if (Option(selectedFileTypes).isDefined) {
      fileTypeToFiles.filter(f => selectedFileTypes(f._1)).foreach { case (fileType, files) =>
        val astNodes = files.map(_.getNode).filter(_ != null)
        if (astNodes.nonEmpty) {
          println(s"File type ${fileType.getName}")
          println(s"AstNodes count: ${astNodes.size}")
          println(s"Free memory ${Runtime.getRuntime.freeMemory()}")

          val start = System.currentTimeMillis()

          val templates = new MB3(
            new MinerConfiguration(minSupportCoefficient = 0.5),
            DefaultSearchConfiguration,
            JavaFileTypeTemplateFilter,
            JavaTemplateProcessor).getTemplates(astNodes)

          println(s"Time for templates extracting: ${System.currentTimeMillis() - start}")
          if (templates.nonEmpty) {
            val importantTemplates = new ChooseImportantTemplates(project, fileType, templates).showDialog()
            importantTemplates.foreach { template =>
               println(template.text)
               println("_____________________________")
            }
//            new TemplatesDialog(project, templates.map(new TemplateWithFileType(_, fileType))).show()
          } else {
            new NoTemplatesDialog(project).show()
          }
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