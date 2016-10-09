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
      tree.add(node)
      validateTreeStructure(tree)
    }
  }

  def validateTreeStructure(sTree: STree): Unit = {
    sTree.idToData.foreach {
      case (id, data) ⇒ id match {
        case i: SLeafNodeId ⇒
          data shouldBe a[SLeafNodeData]
        case i: SInnerNodeId ⇒
          data shouldBe a[SInnerNodeData]
      }
    }

    sTree.idToData.foreach {
      case (id, data) ⇒ (id, data) match {
        case (i: SInnerNodeId, d: SInnerNodeData) ⇒
          i.childrenCount.value shouldEqual d.children.length

          d.children.length should be > 0

          d.children.foreach(_.alternatives.size should be > 0)
        case _ ⇒
      }
    }

    sTree.idToData.values.foreach(_.getOccurrenceCount should be > 0)
  }


  private def buildJavaPsiFiles(texts: String*): Seq[PsiFile] = {
    texts.map(
      createLightFile(JavaFileType.INSTANCE, _)
    )
  }
}