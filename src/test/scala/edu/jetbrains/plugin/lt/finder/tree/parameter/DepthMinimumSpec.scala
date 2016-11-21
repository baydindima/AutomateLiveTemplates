package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class DepthMinimumSpec extends ParameterTestBase {

  def testTooHighMinimumDepth(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.DEPTH_MINIMUM -> 10
        )
      ),
      classText1
    ) shouldBe empty
  }

  def testNormalMinimumDepth(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.DEPTH_MINIMUM -> 5
        )
      ),
      classText1
    ) should not be empty
  }


}
