package edu.jetbrains.plugin.lt.finder.miner

import com.intellij.lang.ASTNode

import scala.collection.mutable

/**
  * Scans tree database.
  * Return dictionary consists of each node in database following the pre-order
  * traversal indexing. For each node its position, label, depth, right-most leaf
  * position (scope), and parent position are stored.
  */
final class DatabaseScanner {

  /**
    * Optimization to maintain only one instance of each node id
    */
  private val nodeIdMap: mutable.Map[NodeId, NodeId] = mutable.Map.empty

  private val nodeIdToCount: mutable.Map[NodeId, Int] = mutable.Map.empty

  private val nodeIdToOccurrencePos: mutable.Map[NodeId, List[Int]] = mutable.Map.empty

  /**
    * Scan tree.
    *
    * @param root of tree
    * @return dictionary consists of each node in database following the
    *         pre-order traversal indexing.
    */
  def getDictionary(root: ASTNode, curPos: Int = 0): Seq[Node] = {
    def addOccurrence(nodeId: NodeId, pos: Int): Unit = {
      nodeIdToCount += (nodeId -> (nodeIdToCount.getOrElse(nodeId, 0) + 1))
      nodeIdToOccurrencePos += (
        nodeId -> (
          pos :: nodeIdToOccurrencePos.getOrElse(nodeId, List.empty[Int])))
    }

    def getNodeId(astNode: ASTNode, childrenCount: Int): NodeId = {
      val nodeId = NodeId(astNode, childrenCount)
      nodeIdMap.getOrElseUpdate(nodeId, nodeId)
    }

    def dfs(curNode: ASTNode, curDepth: Int, parentPos: Int, curPos: Int): Seq[Node] = {
      def next(child: ASTNode, nodesAcc: Seq[Node], childrenCountAcc: Int): (Int, Seq[Node]) = child match {
        case null => (childrenCountAcc, nodesAcc)
        case childNode =>
          val nodes = dfs(
            curNode = childNode,
            curDepth = curDepth + 1,
            parentPos = curPos,
            curPos = curPos + nodesAcc.size)

          next(
            child = childNode.getTreeNext,
            nodesAcc = nodesAcc ++ nodes,
            childrenCountAcc = childrenCountAcc + 1)
      }

      val (childCount, childNodes) = next(
        child = curNode.getFirstChildNode,
        nodesAcc = Vector.empty,
        childrenCountAcc = 0
      )
      val nodeId = getNodeId(curNode, childCount)

      addOccurrence(nodeId, curPos)

      new Node(
        nodeId = nodeId,
        depth = curDepth,
        parentPos = parentPos,
        rightmostLeafPos = curPos + childNodes.size) +: childNodes
    }

    dfs(root, 0, -1, curPos)
  }

  class Node(val nodeId: NodeId,
             val depth: Int,
             val parentPos: Int,
             val rightmostLeafPos: Int)

}
