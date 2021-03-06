package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.common.{InnerNodeId, LeafNodeId}
import edu.jetbrains.plugin.lt.finder.stree.SimTreeBaseSpec._
import edu.jetbrains.plugin.lt.finder.stree.{TestInnerNode => IN, TestLeafNode => LN}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Dmitriy Baidin.
  */
@RunWith(classOf[JUnitRunner])
class SimTreeWithASTGeneratorSpec extends FlatSpec with Matchers {

  it should "unite equal nodes" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS,
        LN(METHOD, "test"),
        LN(METHOD, "test")
      )
    )
    tree.add(root)
    validateTreeStructure(tree)

    tree.idToData.filter {
      case (i, data) => (i, data) match {
        case (i: LeafNodeId, d: SimLeafNodeData) =>
          data.getOccurrenceCount == 2
          i.nodeText == "test"
        case _ => false
      }
    } should have size 1
  }

  it should "add to alternatives different nodes" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS,
        IN(METHOD, LN(BLOCK_STATEMENT, "element1"), LN(BLOCK_STATEMENT, "element2")),
        IN(METHOD, LN(BLOCK_STATEMENT, "element1"), LN(BLOCK_STATEMENT, "element3"))
      )
    )
    tree.add(root)
    validateTreeStructure(tree)

    // TODO: Add test for different parent count

    tree.idToData.size shouldEqual 5
    tree.idToData.values.map(_.getOccurrenceCount).sum shouldEqual 7

    tree.idToData.find {
      case (id, data) =>
        id.elementType == METHOD
    } match {
      case Some((id, data: SimInnerNodeData)) =>
        data.children should have length 2
        data.children(0).alternatives should have size 1
        data.children(0).alternatives should contain key
          LeafNodeId(BLOCK_STATEMENT, "element1")

        data.children(1).alternatives should have size 2
        data.children(1).alternatives should contain key
          LeafNodeId(BLOCK_STATEMENT, "element2")

        data.children(1).alternatives should contain key
          LeafNodeId(BLOCK_STATEMENT, "element3")

      case _ => fail("should find element with this type")
    }
  }

  it should "Add occurrence on each meet" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new SimTree

    val root = generator.gen(
      IN(CLASS, //0
        IN(METHOD, //1
          IN(METHOD_REF_EXPRESSION, //3
            LN(ANNOTATION, "4"), // 4
            LN(BLOCK_STATEMENT, "5") // 5
          ),
          IN(METHOD_CALL_EXPRESSION, // 2
            IN(METHOD_REF_EXPRESSION, // 3
              IN(METHOD_REF_EXPRESSION, // 3
                LN(ANNOTATION, "4"), //4
                LN(BLOCK_STATEMENT, "5")) // 5
            ),
            LN(METHOD, "1") //1
          )
        ),
        IN(METHOD_CALL_EXPRESSION, //2
          LN(METHOD, "1"), //1
          IN(METHOD_REF_EXPRESSION, // 3
            IN(METHOD_CALL_EXPRESSION, // 2
              LN(METHOD, "1"), // 1
              LN(METHOD_REF_EXPRESSION, "3") // 3
            )
          )
        )
      )
    )

    tree.add(root)
    validateTreeStructure(tree)

    tree.idToData should have size 9
    tree.idToData.values.map(_.getOccurrenceCount).sum shouldEqual 17

    // 3
    tree.idToData.get(InnerNodeId(METHOD_REF_EXPRESSION, 2))
      .map(d => d.getOccurrenceCount shouldEqual 2)
      .isDefined shouldEqual true

    // 2
    tree.idToData.get(InnerNodeId(METHOD_CALL_EXPRESSION, 2))
      .map {
        d =>
          d.getOccurrenceCount shouldEqual 3
          d match {
            case d: SimInnerNodeData =>
              // TODO: Add test for alterFreq
              d.children should have length 2
              d.children(0).alternatives should have size 2
              d.children(1).alternatives should have size 3
            case _ => fail("Wrong type of inner node data")
          }
      }
      .isDefined shouldEqual true

    //4
    tree.idToData.get(LeafNodeId(ANNOTATION, "4"))
      .map(d => d.getOccurrenceCount shouldEqual 2)
      .isDefined shouldEqual true
  }

}