package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class MatchesMinimumSpec extends ParameterTestBase {
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

  def testTooHighMatchesMinimum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.MATCHES_MINIMUM → 6
        )
      ),
      classText
    ) should have length 0
  }


  def testNormalMatchesMinimum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.MATCHES_MINIMUM → 5
        )
      ),
      classText
    ) should have length 1
  }

}
