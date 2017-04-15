package edu.jetbrains.plugin.lt.finder.postprocessor

import edu.jetbrains.plugin.lt.finder.miner.{EncodeNode, InnerNodeId, LeafNodeId, PathNode, Placeholder, TreeEncoding}

trait TreeEncodingFormatter {
  def process(treeEncoding: TreeEncoding): (String, TreeEncoding)
}

class DefaultTreeEncodingFormatter extends TreeEncodingFormatter {

  override def process(treeEncoding: TreeEncoding): (String, TreeEncoding) = {
    val encodeList = compressEncodeList(treeEncoding.encodeList)
    (toString(encodeList), TreeEncoding(encodeList))
  }

  protected val compressiblePattern: String = "(\\s|\\n|\\t)*"

  def compressEncodeList(encodeList: List[PathNode]): List[PathNode] = {
    def helper(encodeList: List[PathNode], prevIsPlaceholder: Boolean, accList: List[PathNode]): List[PathNode] = encodeList match {
      case node :: tail =>
        node match {
          case EncodeNode(leaf: LeafNodeId) =>
            if (leaf.nodeText.matches(compressiblePattern))
              if (prevIsPlaceholder)
                helper(tail, prevIsPlaceholder, accList)
              else
                helper(tail, prevIsPlaceholder, node :: accList)
            else
              helper(tail, prevIsPlaceholder = false, node :: accList)
          case EncodeNode(_: InnerNodeId) =>
            helper(tail, prevIsPlaceholder, accList)
          case Placeholder =>
            if (prevIsPlaceholder)
              helper(tail, prevIsPlaceholder = true, accList)
            else
              helper(tail, prevIsPlaceholder = true, node :: accList)
        }
      case Nil => accList
    }

    helper(encodeList, prevIsPlaceholder = false, List.empty)
  }

  def toString(encodeList: List[PathNode]): String = {
    encodeList.map {
      case EncodeNode(leaf: LeafNodeId) =>
        leaf.nodeText
      case Placeholder =>
        " #_# "
      case _ => ""
    }.mkString
  }
}