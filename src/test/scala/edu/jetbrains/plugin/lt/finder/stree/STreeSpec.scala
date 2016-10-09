package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase
import org.scalatest.Matchers

/**
  * Created by Dmitriy Baidin.
  */

class STreeSpec extends LightCodeInsightFixtureTestCase with Matchers {


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

  def testSTreeBuild(): Unit = {
    val tree = new STree
    buildJavaPsiFiles(classText1, classText2).map(_.getNode).foreach { node ⇒
      val start1 = System.currentTimeMillis()
      tree.add(node)
      println(System.currentTimeMillis() - start1)
    }
  }


  private def buildJavaPsiFiles(texts: String*): Seq[PsiFile] = {
    texts.map(
      createLightFile(JavaFileType.INSTANCE, _)
    )
  }
}