package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.stree.{TestInnerNode => IN, TestLeafNode => LN}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Dmitriy Baidin.
  */
@RunWith(classOf[JUnitRunner])
class SimTreeStatSpec extends FlatSpec with Matchers {

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

    val treeStat = tree.calcTreeStatistic

    treeStat.countOfTrees === 1
    treeStat.countOfRoots === 1
    treeStat.nodeCount === 5
    treeStat.innerNodeCount === 3
    treeStat.leavesNodeCount === 2
    treeStat.occurrenceCountOfNode === 14
    treeStat.occurrenceCountOfInnerNode === 6
    treeStat.occurrenceCountOfLeafNode === 8
    treeStat.maxOccurrenceCount === 6
    treeStat.maxCountOfAlternatives === 2
  }

}
