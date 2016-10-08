package edu.jetbrains.plugin.lt.finder.tree.parameter

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.fileTypes.FileType
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import edu.jetbrains.plugin.lt.finder.Parameters.Name
import edu.jetbrains.plugin.lt.finder.tree.{TreeTemplatesFinder, TreeTemplatesFinderParameters}
import edu.jetbrains.plugin.lt.finder.{Parameters, Template}
import org.scalatest.Matchers

import scala.collection.JavaConverters._

/**
  * Created by Dmitriy Baidin.
  */
abstract class ParameterTestBase extends LightCodeInsightFixtureTestCase with Matchers {

  val classText2 =
    """
      |package edu.jetbrains.plugin.lt.finder.tree;
      |
      |
      |import java.util.ArrayList;
      |import java.util.List;
      |
      |class Test {
      |
      |    public static void main(String[] args) {
      |        List<Integer> ints = new ArrayList<>();
      |        for (String arg : args) {
      |            ints.add(Integer.valueOf(arg));
      |        }
      |
      |        List<Double> doubles = new ArrayList<>();
      |        for (String arg : args) {
      |            doubles.add(Double.valueOf(arg));
      |        }
      |
      |        List<Float> floats = new ArrayList<>();
      |        for (String arg : args) {
      |            floats.add(Float.valueOf(arg));
      |        }
      |
      |        List<Short> shorts = new ArrayList<>();
      |        for (String arg : args) {
      |            shorts.add(Short.valueOf(arg));
      |        }
      |    }
      |}
      | """
  val classText1 =
    """
      |package edu.jetbrains.plugin.lt.finder.tree;
      |
      |
      |class Test {
      |
      |    public static void main(String[] args) {
      |        System.out.println("a");
      |        System.out.println("b");
      |        System.out.println("c");
      |        System.out.println("d");
      |        System.out.println("e");
      |    }
      |}
    """
  private val mockParameters = Map(
    TreeTemplatesFinderParameters.Name.DEPTH_MINIMUM → 0,
    TreeTemplatesFinderParameters.Name.LENGTH_MAXIMUM → Int.MaxValue,
    TreeTemplatesFinderParameters.Name.MATCHES_MINIMUM → 2,
    TreeTemplatesFinderParameters.Name.LENGTH_MINIMUM → 0,
    TreeTemplatesFinderParameters.Name.PLACEHOLDERS_MAXIMUM → Int.MaxValue,
    TreeTemplatesFinderParameters.Name.TEMPLATES_TO_SHOW → Int.MaxValue
  )

  def getSearchParameters(params: Map[Name, Int] = Map.empty): Parameters = {
    val finderParameters = new TreeTemplatesFinderParameters
    mockParameters.foreach(p ⇒ finderParameters.setParameter(p._1, p._2))
    params.foreach(p ⇒ finderParameters.setParameter(p._1, p._2))
    finderParameters
  }

  def findJavaTemplates(finderParameters: Parameters, texts: String*): Seq[Template] = {
    new TreeTemplatesFinder(
      texts.map(
        createLightFile(JavaFileType.INSTANCE, _)
      ).asJava,
      finderParameters
    ).analyze().asScala
  }

  def findTemplates(finderParameters: Parameters, fileType: FileType, texts: String*): Seq[Template] = {
    new TreeTemplatesFinder(
      texts.map(
        createLightFile(fileType, _)
      ).asJava,
      finderParameters
    ).analyze().asScala
  }

}
