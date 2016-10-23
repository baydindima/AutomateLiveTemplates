package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree._

import scala.collection.mutable

/**
  * Created by Dmitriy Baidin.
  */
class TemplateSearcher(val simTree: SimTree) {

  val nodeIdToTemplateNode: mutable.HashMap[NodeId, TemplateNode] = mutable.HashMap.empty

  val possibleTemplateRoot: mutable.Set[TemplateNode] = mutable.Set.empty

  private def addTemplateNodes(nodes: Seq[SimNode]) = {
    nodes.foreach {
      case SimInnerNode(id, data) ⇒
        val templateNode = new TemplateInnerNode(
          nodeId = id,
          children = getMostLikelyChildren(data.children),
          generalInnerNodeStatistic = GeneralInnerNodeStatistic(
            childrenCount = id.childrenCount.value,
            occurrenceCount = data.getOccurrenceCount,
            differentParentCount = data.getDifferentParentCount,
            innerNodeStatistics = data.statistics
          )
        )
        nodeIdToTemplateNode += (id → templateNode)
      case SimLeafNode(id, data) ⇒
        val templateNode = new TemplateLeafNode(
          nodeId = id,
          generalLeafStatistic = GeneralLeafNodeStatistic(
            occurrenceCount = data.getOccurrenceCount,
            differentParentCount = data.getDifferentParentCount,
            leafNodeStatistics = data.statistics
          )
        )
        nodeIdToTemplateNode += (id → templateNode)
    }
  }

  private def getMostLikelyChildren(alternatives:
                                    Array[NodeChildrenAlternatives]): Array[TemplateNode] = ???


}

/**
  * Find all candidates for template root, calculate most likely variant. Possible status (root, none root)
  * just iterate over idToMap
  * Try top-sort all from possible root, update parentLink
  * If find cycle (for now just remove from templates)
  * Then try dfs from top
  * Calculate score of templates
  *
  *
  *
  * Test for templates, filter less general variant
  *
  */
