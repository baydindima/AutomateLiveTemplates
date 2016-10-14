package edu.jetbrains.plugin.lt.finder.stree

import scala.collection.mutable

/**
  * Created by Dmitriy Baidin.
  */
class StatTree(private val simTree: SimTree) {
  val visited: mutable.Set[NodeId] = mutable.HashSet.empty
  val idToStat: mutable.Map[NodeId, StatNode] = mutable.HashMap.empty

  calcStatistics()

  private def calcStatistics(): Unit = {
    for (root ← simTree.rootNodes) {
      dfsSimTree(root, PreStat.empty)
    }
  }

  private def dfsSimTree(simNode: SimNode, parentPreStat: PreStat): StatNode = visited(simNode.nodeId) match {
    case false ⇒

      visited.add(simNode.nodeId)

      val result = simNode match {
        case leafNode: SimLeafNode ⇒
          new StatLeafNode(
            nodeId = leafNode.nodeId,
            new StatLeafNodeData(
              calcLeafStatistics(leafNode, parentPreStat)
            )
          )
        case innerNode: SimInnerNode ⇒
          val preStat = calcPreStatistics(innerNode, parentPreStat)
          val simChildren = innerNode.data.children
          val children: Array[NodeChildrenAlternatives[StatNode]] = new Array(simChildren.length)

          for {
            i ← children.indices
            childAlternatives = NodeChildrenAlternatives[StatNode]()
          } {

            simChildren(i).alternatives.foreach {
              case (id, childNode) ⇒
                val childStat = dfsSimTree(childNode, preStat)
                childAlternatives.alternatives += (id → childStat)
            }

            children(i) = childAlternatives
          }

          new StatInnerNode(
            innerNode.nodeId,
            new StatInnerNodeData(calcInnerStatistics(innerNode, children, parentPreStat), children)
          )
      }
      idToStat += (simNode.nodeId → result)
      result
    case true ⇒
      idToStat.get(simNode.nodeId) match {
        case Some(statNode) ⇒ statNode
        case None ⇒
          //          TODO: Add logging
          //          println("Cyclic dependency in stat tree!")
          //          return empty node
          throw new RuntimeException("Cyclic dependency in stat tree!")
      }
  }

  private def calcLeafStatistics(leafNode: SimLeafNode, preStat: PreStat): StatData = {
    StatData(
      maxHeight = 0,
      depth = preStat.depth + 1
    )
  }

  private def calcPreStatistics(curNode: SimInnerNode, parentPreStat: PreStat): PreStat = {
    new PreStat(
      depth = parentPreStat.depth + 1
    )
  }

  private def calcInnerStatistics(innerNode: SimInnerNode,
                                  childrenStatistics: Array[NodeChildrenAlternatives[StatNode]],
                                  parentPreStat: PreStat): StatData = {
    val allChildrenStat = childrenStatistics.flatMap(alt ⇒ alt.alternatives.values).toSet
    StatData(
      maxHeight = allChildrenStat.map(_.data.statData.maxHeight).max + 1,
      depth = parentPreStat.depth + 1
    )
  }

  case class PreStat(depth: Int)

  object PreStat {
    def empty: PreStat = PreStat(depth = 0)
  }

}
