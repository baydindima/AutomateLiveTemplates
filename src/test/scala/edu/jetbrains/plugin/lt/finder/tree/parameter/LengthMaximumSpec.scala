package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */

class LengthMaximumSpec extends ParameterTestBase {
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
      |    }
      |}
    """

  def testTooLowMaximumLength(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 10
        )
      ),
      classText
    ) should have length 0
  }

  def testNormalMaximumLength(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 1488
        )
      ),
      classText
    ) should have length 1
  }


}
