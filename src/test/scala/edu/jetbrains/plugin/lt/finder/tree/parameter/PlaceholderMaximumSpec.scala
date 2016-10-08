package edu.jetbrains.plugin.lt.finder.tree.parameter

import edu.jetbrains.plugin.lt.finder.tree.TreeTemplatesFinderParameters.Name

/**
  * Created by Dmitriy Baidin.
  */
class PlaceholderMaximumSpec extends ParameterTestBase {
  val classText =
    """
      |package edu.jetbrains.plugin.lt.finder.tree;
      |
      |
      |import java.util.ArrayList;
      |import java.util.List;
      |
      |class Test {
      |
      |    public static void main(String[] args) {
      |        List<Integer> ints = new ArrayList<>();
      |        for (String arg : args) {
      |            ints.add(Integer.valueOf(arg));
      |        }
      |
      |        List<Double> doubles = new ArrayList<>();
      |        for (String arg : args) {
      |            doubles.add(Double.valueOf(arg));
      |        }
      |
      |        List<Float> floats = new ArrayList<>();
      |        for (String arg : args) {
      |            floats.add(Float.valueOf(arg));
      |        }
      |
      |        List<Short> shorts = new ArrayList<>();
      |        for (String arg : args) {
      |            shorts.add(Short.valueOf(arg));
      |        }
      |    }
      |}
      | """

  def testTooLowPlaceholderMaximum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.PLACEHOLDERS_MAXIMUM → 1
        )
      ),
      classText
    ) shouldBe empty
  }

  def testNormalPlaceholderMaximum(): Unit = {
    findJavaTemplates(
      getSearchParameters(
        Map(
          Name.PLACEHOLDERS_MAXIMUM → 3
        )
      ),
      classText
    ) should not be empty
  }

}
