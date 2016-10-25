package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree.{ASTGenerator, SimTree, ChildrenCount => CC, ElementType => ET, TestInnerNode => IN, TestLeafNode => LN}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Dmitriy Baidin.
  */
@RunWith(classOf[JUnitRunner])
class TemplateSearcherSpec extends FlatSpec with Matchers {

  it should "return templates " in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS, //0
        IN(METHOD, //1
          LN(BLOCK_STATEMENT, "4"), // 3
          LN(ANNOTATION, "3") // 4
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          IN(METHOD, //1
            LN(BLOCK_STATEMENT, "4"), // 3
            LN(ANNOTATION, "3") // 4
          ),
          LN(ANNOTATION, "3") // 3
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          IN(METHOD, //1
            LN(BLOCK_STATEMENT, "4"), // 3
            LN(ANNOTATION, "3") // 4
          ),
          LN(ANNOTATION, "3") // 3
        )
      )
    )
    tree.add(root)
    val templateSearcher = new TemplateSearcher(tree)
    val templates = templateSearcher.searchTemplate

    templates.map(_.text).head === "4 3"
  }

  it should "return more general template" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS, //0
        IN(METHOD_CALL_EXPRESSION, //2
          IN(METHOD, //1
            LN(BLOCK_STATEMENT, "4"), // 3
            LN(ANNOTATION, "3") // 4
          ),
          LN(ANNOTATION, "3") // 3
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          IN(METHOD, //1
            LN(BLOCK_STATEMENT, "4"), // 3
            LN(ANNOTATION, "3") // 4
          ),
          LN(ANNOTATION, "3") // 3
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          IN(METHOD, //1
            LN(BLOCK_STATEMENT, "4"), // 3
            LN(ANNOTATION, "3") // 4
          ),
          LN(ANNOTATION, "3") // 3
        )
      )
    )
    tree.add(root)
    val templateSearcher = new TemplateSearcher(tree)
    val templates = templateSearcher.searchTemplate

    templates should have size 1
    templates.map(_.text).head === "4 3 3"
  }

}
