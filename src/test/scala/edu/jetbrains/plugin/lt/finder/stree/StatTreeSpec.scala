package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.stree.{TestInnerNode => IN, TestLeafNode => LN}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Dmitriy Baidin.
  */
@RunWith(classOf[JUnitRunner])
class StatTreeSpec extends FlatSpec with Matchers {

  it should "calc statistics" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS, //0
        IN(METHOD, //1
          LN(ANNOTATION, "3"), // 3
          LN(BLOCK_STATEMENT, "4") // 4
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          LN(ANNOTATION, "3"), // 3
          IN(METHOD, //1
            LN(ANNOTATION, "3"), // 3
            LN(ANNOTATION, "3") // 3
          )
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
    /*
        tree.rootNodes should have size 1
        tree.idToData.get(InnerNodeId(ET(CLASS), CC(3)))
          .map { node =>
            node.getOccurrenceCount shouldEqual 1
            node.getDifferentParentCount shouldEqual 1
            node match {
              case data: SimInnerNodeData =>
                data.statistic should have size 1
                val stat = data.statistic.head
                stat.nodeCount shouldEqual 13
                stat.leafCount shouldEqual 8
                stat.innerCount shouldEqual 5
                stat.maxHeight shouldEqual 3
                stat.minHeight shouldEqual 2
                stat.commonStatistic.depth shouldEqual 0
                stat.commonStatistic.siblingsCount shouldEqual 0
              case _ => fail
            }
          }.isDefined shouldEqual true

        tree.idToData.get(InnerNodeId(ET(METHOD_CALL_EXPRESSION), CC(2)))
          .map { node =>
            node.getOccurrenceCount shouldEqual 2
            node.getDifferentParentCount shouldEqual 2
            node match {
              case data: SimInnerNodeData =>
                data.statistic should have size 2
                val stat = data.statistic.head
                stat.nodeCount === 4
                stat.leafCount === 3
                stat.innerCount === 1
                stat.maxHeight === 2
                stat.minHeight === 1
                stat.commonStatistic.depth === 1
                stat.commonStatistic.siblingsCount === 2
              case _ => fail
            }
          }.isDefined shouldEqual true

        tree.idToData.get(LeafNodeId(ET(ANNOTATION), NodeText("3")))
          .map { node =>
            node.getOccurrenceCount === 6
            node.getDifferentParentCount === 4
            node match {
              case data: SimLeafNodeData =>
                data.statistic should have size 6
                val stat = data.statistic.head
                stat.commonStatistic.depth === 2
                stat.commonStatistic.siblingsCount === 1
                stat.textLength === 1
              case _ => fail
            }
          }.isDefined shouldEqual true*/
  }

}
