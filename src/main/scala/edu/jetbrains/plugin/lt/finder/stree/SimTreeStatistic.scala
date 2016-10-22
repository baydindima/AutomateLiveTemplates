package edu.jetbrains.plugin.lt.finder.stree


/**
  * Statistic for generalized AST-Tree
  */
class SimTreeStatistic(val countOfTrees: Int,
                       val countOfRoots: Int,
                       val innerNodeCount: Int,
                       val occurrenceCountOfInnerNode: Int,
                       val leavesNodeCount: Int,
                       val occurrenceCountOfLeafNode: Int,
                       val nodeCount: Int,
                       val occurrenceCountOfNode: Int,
                       val maxCountOfAlternatives: Int,
                       val averageCountOfAlternatives: Double,
                       val maxOccurrenceCount: Int,
                       val averageOccurrenceCount: Double) {

}
