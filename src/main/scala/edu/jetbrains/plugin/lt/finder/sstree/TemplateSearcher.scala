package edu.jetbrains.plugin.lt.finder.sstree

/**
  * Created by Dmitriy Baidin.
  */
class TemplateSearcher(configuration: TemplateSearchConfiguration) {

  def getPossibleTemplateRoots(sufSimTree: SufSimTree): List[NodeInfo] =
    sufSimTree.treeRoot.values.filter(configuration.isPossibleTemplateRoot).toList


}
