package edu.jetbrains.plugin.lt.finder.stree

import com.intellij.lang.ASTNode

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by Dmitriy Baidin.
  */
class STree {

  private val sNodeMap: mutable.Map[SNodeInfo, SNodeLink] = mutable.Map.empty

  def add(astNode: ASTNode): Unit = {
    add(astNode, None)
  }

  def printTree(): Seq[String] = {
    sNodeMap.filterKeys {
      case i: SLeafNodeInfo ⇒ true
      case _ ⇒ false
    }.map {
      case (i: SLeafNodeInfo, link) ⇒
        s"${i.nodeText.value}: ${link.getOccurrenceCount}"
    }.toSeq
  }

  private def add(astNode: ASTNode, parent: Option[SNodeChildrenAlternatives]): Unit = {
    val (childrenCount, children) = getChildren(astNode)
    val nodeInfo = getInfo(astNode, childrenCount)

    val linkToNode = updateSNodeMap(nodeInfo, childrenCount)

    parent.map(_.alternatives.put(nodeInfo, linkToNode))

    linkToNode match {
      case link: InnerSLink ⇒
        children.zip(link.children).foreach { case (child, alternatives) ⇒
          add(child, Some(alternatives))
        }
      case _ ⇒
    }
  }

  private def updateSNodeMap(nodeInfo: SNodeInfo, childrenCount: Int): SNodeLink = sNodeMap.get(nodeInfo) match {
    case Some(link) ⇒
      link.addOccurrence()
      link
    case None ⇒
      val link = buildLink(childrenCount)
      sNodeMap.put(nodeInfo, link)
      link
  }

  private def buildLink(childrenCount: Int): SNodeLink = childrenCount match {
    case 0 ⇒
      new LeafSLink
    case n ⇒
      new InnerSLink(Array.fill(n) {
        SNodeChildrenAlternatives()
      })
  }

  private def getChildren(astNode: ASTNode): (Int, List[ASTNode]) = {
    var child = astNode.getFirstChildNode

    var childrenCount = 0
    var result: List[ASTNode] = List.empty

    while (child != null) {
      childrenCount += 1
      result ::= child
      child = child.getTreeNext
    }

    (childrenCount, result)
  }

  private def getInfo(astNode: ASTNode, childrenCount: Int): SNodeInfo = childrenCount match {
    case 0 ⇒
      SLeafNodeInfo(
        IElementTypeIndex(astNode.getElementType.getIndex),
        NodeText(astNode.getText))
    case n ⇒
      SInnerNodeInfo(
        IElementTypeIndex(astNode.getElementType.getIndex),
        ChildrenCount(n)
      )
  }


}
