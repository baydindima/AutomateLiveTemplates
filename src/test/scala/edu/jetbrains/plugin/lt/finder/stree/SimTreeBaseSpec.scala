package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.common.{InnerNodeId, LeafNodeId}
import org.scalatest.Matchers

/**
  * Created by Dmitriy Baidin.
  */
object SimTreeBaseSpec extends Matchers {
  def validateTreeStructure(sTree: SimTree): Unit = {
    sTree.idToData.foreach {
      case (id, data) => id match {
        case i: LeafNodeId =>
          data shouldBe a[SimLeafNodeData]
        case i: InnerNodeId =>
          data shouldBe a[SimInnerNodeData]
      }
    }

    sTree.idToData.foreach {
      case (id, data) => (id, data) match {
        case (i: InnerNodeId, d: SimInnerNodeData) =>
          i.childrenCount shouldEqual d.children.length

          d.children.length should be > 0

          d.children.foreach(_.alternatives.size should be > 0)
        case _ =>
      }
    }

    sTree.idToData.values.foreach(_.getOccurrenceCount should be > 0)
  }
}
