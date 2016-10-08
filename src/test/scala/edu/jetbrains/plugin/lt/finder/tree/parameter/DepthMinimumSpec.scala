package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class DepthMinimumSpec extends ParameterTestBase {
  val classText =
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

  def testTooHighMinimumDepth(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.DEPTH_MINIMUM → 10
        )
      ),
      classText
    ) shouldBe empty
  }

  def testNormalMinimumDepth(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.DEPTH_MINIMUM → 5
        )
      ),
      classText
    ) should not be empty
  }


}
