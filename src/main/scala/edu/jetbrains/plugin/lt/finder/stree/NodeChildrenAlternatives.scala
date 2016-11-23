package edu.jetbrains.plugin.lt.finder.stree

import edu.jetbrains.plugin.lt.finder.common.NodeId

import scala.collection.mutable

/**
  * Class for representing alternatives of children
  *
  * @param alternatives map which store info about node 2 link
  */
class NodeChildrenAlternatives(val alternatives: mutable.Map[NodeId, SimNode],
                               val alternativeFrequency: mutable.Map[NodeId, Int]) {
  def putIfAbsent(node: SimNode): Boolean = {
    alternatives.get(node.nodeId) match {
      case Some(n) =>
        alternativeFrequency(node.nodeId) = alternativeFrequency(node.nodeId) + 1
        false
      case None =>
        alternativeFrequency.put(node.nodeId, 1)
        alternatives.put(node.nodeId, node)
        true
    }
  }
}

object NodeChildrenAlternatives {
  def apply(): NodeChildrenAlternatives = new NodeChildrenAlternatives(
    new mutable.HashMap[NodeId, SimNode](),
    new mutable.HashMap[NodeId, Int]())
}