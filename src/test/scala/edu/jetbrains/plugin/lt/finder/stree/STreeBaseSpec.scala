package edu.jetbrains.plugin.lt.finder.stree

import org.scalatest.Matchers

/**
  * Created by Dmitriy Baidin.
  */
object STreeBaseSpec extends Matchers {
  def validateTreeStructure(sTree: STree): Unit = {
    sTree.idToData.foreach {
      case (id, data) ⇒ id match {
        case i: SLeafNodeId ⇒
          data shouldBe a[SLeafNodeData]
        case i: SInnerNodeId ⇒
          data shouldBe a[SInnerNodeData]
      }
    }

    sTree.idToData.foreach {
      case (id, data) ⇒ (id, data) match {
        case (i: SInnerNodeId, d: SInnerNodeData) ⇒
          i.childrenCount.value shouldEqual d.children.length

          d.children.length should be > 0

          d.children.foreach(_.alternatives.size should be > 0)
        case _ ⇒
      }
    }

    sTree.idToData.values.foreach(_.getOccurrenceCount should be > 0)
  }
}
