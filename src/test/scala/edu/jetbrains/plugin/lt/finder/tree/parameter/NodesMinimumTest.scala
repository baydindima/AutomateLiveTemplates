package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class NodesMinimumTest extends ParameterTestBase {
  def testTooHighMinimumNodes(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.NODES_MINIMUM -> 30
        )
      ),
      classText1
    ) shouldBe empty
  }

  def testNormalMinimumNode(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.NODES_MINIMUM -> 5
        )
      ),
      classText1
    ) should not be empty
  }


}
