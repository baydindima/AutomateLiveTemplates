package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.stree.STreeBaseSpec._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by Dmitriy Baidin.
  */
@RunWith(classOf[JUnitRunner])
class STreeWithASTGeneratorSpec extends FlatSpec with Matchers {


  it should "unite equal nodes" in {
    import com.intellij.psi.impl.source.tree.JavaElementType._
    val generator = new ASTGenerator
    val tree = new STree

    val root = generator.gen(
      TestInnerNode(CLASS,
        TestLeafNode(METHOD, "test"),
        TestLeafNode(METHOD, "test")
      )
    )
    tree.add(root)
    validateTreeStructure(tree)

    tree.idToData.filter {
      case (i, data) ⇒ (i, data) match {
        case (i: SLeafNodeId, d: SLeafNodeData) ⇒
          data.getOccurrenceCount == 2
          i.nodeText.value == "test"
        case _ ⇒ false
      }
    } should have size 1
  }
}