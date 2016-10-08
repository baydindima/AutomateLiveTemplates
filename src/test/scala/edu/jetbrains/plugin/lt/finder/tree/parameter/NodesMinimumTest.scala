package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class NodesMinimumTest extends ParameterTestBase {
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

  def testTooHighMinimumNodes(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.NODES_MINIMUM → 30
        )
      ),
      classText
    ) shouldBe empty
  }

  def testNormalMinimumNode(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.NODES_MINIMUM → 5
        )
      ),
      classText
    ) should not be empty
  }


}
