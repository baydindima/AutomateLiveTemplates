package edu.jetbrains.plugin.lt.finder.tree.parameter

import com.intellij.ide.highlighter.JavaFileType
import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name
import org.intellij.lang.annotations.Language
import org.scalatest.Matchers

/**
  * Created by Dmitriy Baidin.
  */

class LengthMaximumSpec extends ParameterTestUtils with Matchers {

  @Language("Java")
  val classText =
    """
      |package edu.jetbrains.plugin.lt.finder.tree;
      |
      |
      |class LengthMinimumTestFile {
      |
      |    public static void main(String[] args) {
      |        System.out.println("a");
      |        System.out.println("b");
      |        System.out.println("c");
      |        System.out.println("d");
      |        System.out.println("e");
      |        System.out.println("f");
      |        System.out.println("g");
      |        System.out.println("h");
      |        System.out.println("i");
      |        System.out.println("g");
      |        System.out.println("k");
      |        System.out.println("l");
      |        System.out.println("m");
      |        System.out.println("n");
      |    }
      |}
    """

  def testTooLowMaximumLength(): Unit = {
    findTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 10
        )
      ),
      JavaFileType.INSTANCE,
      classText
    ) should have length 0
  }

  def testNormalMaximumLength(): Unit = {
    findTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 1488
        )
      ),
      JavaFileType.INSTANCE,
      classText
    ) should have length 1
  }


}
