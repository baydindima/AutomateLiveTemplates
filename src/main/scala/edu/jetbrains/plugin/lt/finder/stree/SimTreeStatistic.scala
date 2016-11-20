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
  override def toString: String =
    s"""
       |Count of trees:                     $countOfTrees
       |Count of roots:                     $countOfRoots
       |Inner node count:                   $innerNodeCount
       |Occurrence count of inner nodes:    $occurrenceCountOfInnerNode
       |Leaf node count:                    $leavesNodeCount
       |Occurrence count of leaf nodes:     $occurrenceCountOfLeafNode
       |Node count:                         $nodeCount
       |Occurrence count of nodes:          $occurrenceCountOfNode
       |Max count of alternatives:          $maxCountOfAlternatives
       |Average count of alternatives:      $averageCountOfAlternatives
       |Max occurrence count:               $maxOccurrenceCount
       |Average occurrence count:           $averageOccurrenceCount
    """.stripMargin
}
