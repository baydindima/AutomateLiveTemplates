package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */

class LengthMaximumSpec extends ParameterTestBase {

  def testTooLowMaximumLength(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 10
        )
      ),
      classText1
    ) shouldBe empty
  }

  def testNormalMaximumLength(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.LENGTH_MAXIMUM → 1488
        )
      ),
      classText1
    ) should not be empty
  }


}
