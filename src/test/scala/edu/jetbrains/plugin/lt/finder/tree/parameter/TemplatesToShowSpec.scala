package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class TemplatesToShowSpec extends ParameterTestBase {
  def test(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.TEMPLATES_TO_SHOW → 1
        )
      ),
      classText1,
      classText2
    ) should have length 1


    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.TEMPLATES_TO_SHOW → 2
        )

      ),
      classText1,
      classText2
    ) should have length 2
  }

}
