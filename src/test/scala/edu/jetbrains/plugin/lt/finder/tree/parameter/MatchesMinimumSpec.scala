package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class MatchesMinimumSpec extends ParameterTestBase {

  def testTooHighMatchesMinimum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.MATCHES_MINIMUM → 6
        )
      ),
      classText1
    ) shouldBe empty
  }


  def testNormalMatchesMinimum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.MATCHES_MINIMUM → 5
        )
      ),
      classText1
    ) should not be empty
  }

}
