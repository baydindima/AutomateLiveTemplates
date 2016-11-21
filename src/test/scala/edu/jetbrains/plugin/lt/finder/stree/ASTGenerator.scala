package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.lang.ASTNode
import com.intellij.psi.tree.IElementType
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec


/**
  * Created by Dmitriy Baidin.
  */
class ASTGenerator extends FlatSpec with MockFactory {

  def gen(testNode: TestNode): ASTNode = {
    buildNode(testNode, Seq())
  }

  private def buildLeafNode(currentLeaf: TestLeafNode, siblings: Seq[TestNode]): ASTNode = {
    val elem = stub[ASTNode]
    elem.getText _ when() returning currentLeaf.text
    elem.getElementType _ when() returning currentLeaf.elementType

    elem.getTreeNext _ when() returning {
      siblings.headOption match {
        case Some(head) =>
          buildNode(head, siblings.tail)
        case None =>
          null
      }
    }

    elem
  }

  private def buildInnerNode(currentNode: TestInnerNode, siblings: Seq[TestNode]): ASTNode = {
    val elem = stub[ASTNode]
    elem.getElementType _ when() returning currentNode.elementType
    elem.getFirstChildNode _ when() returning buildNode(currentNode.children.head, currentNode.children.tail)

    elem.getTreeNext _ when() returning {
      siblings.headOption match {
        case Some(head) =>
          buildNode(head, siblings.tail)
        case None =>
          null
      }
    }

    elem
  }

  private def buildNode(testNode: TestNode, siblings: Seq[TestNode]): ASTNode = testNode match {
    case t: TestInnerNode => buildInnerNode(t, siblings)
    case t: TestLeafNode => buildLeafNode(t, siblings)
  }
}


sealed abstract class TestNode

case class TestInnerNode(elementType: IElementType, children: TestNode*) extends TestNode

case class TestLeafNode(elementType: IElementType, text: String) extends TestNode