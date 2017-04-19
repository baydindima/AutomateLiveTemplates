package edu.jetbrains.plugin.lt.finder.miner

import java.io.{File, FileFilter}

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import edu.jetbrains.plugin.lt.finder.extensions.JavaFileTypeNodeFilter
import edu.jetbrains.plugin.lt.finder.postprocessor.{DefaultTemplatePostProcessor, DefaultTreeEncodingFormatter, JavaTreeEncodingFormatter}
import edu.jetbrains.plugin.lt.finder.sstree.{DefaultSearchConfiguration, TemplateFilter}
import org.scalatest.Matchers

import scala.io.Source

class MB3LargeTest extends LightCodeInsightFixtureTestCase with Matchers {

  private def buildJavaPsiFiles(texts: String*): Seq[PsiFile] = {
    texts.map(
      createLightFile(JavaFileType.INSTANCE, _)
    )
  }

  def test(): Unit = {
    def getJavaFiles(file: File): Seq[File] = {
      if (file.isDirectory) {
        file.listFiles(new FileFilter() {
          override def accept(pathname: File): Boolean = pathname.isDirectory || pathname.getName.endsWith(".java")
        }).flatMap(getJavaFiles)
      } else {
        Seq(file)
      }
    }

    def readFile(file: File): String = Source.fromFile(file).getLines().mkString("\n")

    val dir = new File("./src/test/resources/src")
    val javaFiles = getJavaFiles(dir)
    val texts = javaFiles.map(readFile)
    val psiFiles = buildJavaPsiFiles(texts: _*)

    val templateFilterImpl = new TemplateFilter(DefaultSearchConfiguration)
    val templateProcessor = new DefaultTemplatePostProcessor {

      override protected val templateFilter: TemplateFilter = templateFilterImpl

      override protected val treeEncodingFormatter = new JavaTreeEncodingFormatter
    }

    val templates = templateProcessor.process(new MB3(
      new MinerConfiguration(1),
      new JavaFileTypeNodeFilter).getFrequentTreeEncodings(psiFiles.map(_.getNode)))

    templates.foreach{template =>
      println(template.text)
      println("__________________________________")
    }
  }

}
