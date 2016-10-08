package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class PlaceholderMaximumSpec extends ParameterTestBase {

  def testTooLowPlaceholderMaximum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.PLACEHOLDERS_MAXIMUM → 1
        )
      ),
      classText2
    ) shouldBe empty
  }

  def testNormalPlaceholderMaximum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.PLACEHOLDERS_MAXIMUM → 3
        )
      ),
      classText2
    ) should not be empty
  }

}
