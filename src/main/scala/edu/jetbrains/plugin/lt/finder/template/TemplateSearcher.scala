package edu.jetbrains.plugin.lt.finder.template

import edu.jetbrains.plugin.lt.finder.stree._

import scala.collection.mutable

/**
  * Created by Dmitriy Baidin.
  */
class TemplateSearcher(val simTree: SimTree) {

  val nodeIdToTemplateNode: mutable.HashMap[NodeId, TemplateNode] = mutable.HashMap.empty

  val possibleTemplateRoot: mutable.Set[TemplateNode] = mutable.Set.empty

  val templateNodeToParent: mutable.Map[TemplateNode, Seq[TemplateNode]] = mutable.HashMap.empty

  def searchTemplate: Seq[Template] = {
    addTemplateNodes(
      simTree.idToData.map {
        case (id, data) ⇒
          SimNode(id, data)
      }.toSeq
    )
    possibleTemplateRoot ++= nodeIdToTemplateNode.values.filter(isPossibleTemplateRoot)

    val templateWithRoots = getTemplateWithRoot.filter(_.template.templateStatistic.placeholderCount < 5)
    val validTemplates: Set[TemplateNode] = templateWithRoots.map(_.templateNode).toSet

    def containParent(templateNode: TemplateNode): Boolean = {
      templateNodeToParent.get(templateNode).exists(nodes ⇒ nodes.exists(validTemplates) || nodes.exists(containParent))
    }

    templateWithRoots.filter(template ⇒ !containParent(template.templateNode)).map(_.template).sortBy(_.text.length)
  }


  private def addTemplateNodes(nodes: Seq[SimNode]) = {
    def getChildrenStub(childrenCount: Int): Array[TemplateNode] =
      new Array(childrenCount)

    nodes.foreach {
      case SimInnerNode(id, data) ⇒
        val templateNode = new TemplateInnerNode(
          nodeId = id,
          children = getChildrenStub(data.children.length),
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

  private def isPossibleTemplateRoot(templateNode: TemplateNode): Boolean = templateNode match {
    case node: TemplateInnerNode ⇒
      node.generalInnerNodeStatistic.commonStatistic.occurrenceCount >= 3
    case _ ⇒ false
  }

  private def getTemplateWithRoot: Seq[TemplateWithRoot] = {
    val tempMark: mutable.Set[TemplateNode] = mutable.HashSet.empty
    val templateNodeToTemplate: mutable.Map[TemplateNode, Template] = mutable.HashMap.empty

    def dfs(templateNode: TemplateNode, parent: Option[TemplateInnerNode]): Template = {
      if (tempMark(templateNode)) {
        return PlaceholderTemplate
      }
      if (possibleTemplateRoot(templateNode) && parent.isDefined) {
        templateNodeToParent += (templateNode → (parent.get +: templateNodeToParent.getOrElse(templateNode, List.empty)))
      }
      templateNodeToTemplate.get(templateNode) match {
        case Some(template) ⇒ template
        case None ⇒
          templateNode match {
            case leaf: TemplateLeafNode ⇒
              val template = new Template(leaf.nodeId.asInstanceOf[LeafNodeId].nodeText.value, new TemplateStatistic(0))
              templateNodeToTemplate += (leaf → template)
              template
            case inner: TemplateInnerNode ⇒
              tempMark += inner
              setMostLikelyChildren(inner.children, simTree.idToData(inner.nodeId).asInstanceOf[SimInnerNodeData].children)
              val nextParent = if (possibleTemplateRoot(inner)) inner else parent.get
              val template = Template(inner.children.map(dfs(_, Some(nextParent))))
              templateNodeToTemplate += (inner → template)
              tempMark -= inner
              template
            case TemplatePlaceholder ⇒ PlaceholderTemplate
          }
      }
    }

    possibleTemplateRoot.map(node ⇒ new TemplateWithRoot(node, dfs(node, None))).toSeq
  }

  private def setMostLikelyChildren(children: Array[TemplateNode],
                                    alternatives: Array[NodeChildrenAlternatives]): Unit = {
    for (i ← children.indices) {
      val alternative = alternatives(i)
      val totalCount = alternative.alternativeFrequency.values.sum
      val max = alternative.alternativeFrequency.maxBy(_._2)

      children(i) = if (max._2 >= 3 && max._2 >= totalCount * 0.75) {
        nodeIdToTemplateNode(max._1)
      } else {
        TemplatePlaceholder
      }
    }
  }

  class TemplateWithRoot(val templateNode: TemplateNode,
                         val template: Template)


}

/**
  * Find all candidates for template root, calculate most likely variant. Possible status (root, none root)
  * just iterate over idToMap
  * Try top-sort all from possible root, update parentLink
  * If find cycle add to cyclicNodes (for now just remove from templates)
  * Then try dfs from top
  * Calculate score of templates
  *
  *
  *
  * Test for templates, filter less general variant, sort by score
  *
  */
