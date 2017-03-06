package edu.jetbrains.plugin.lt.finder.miner


import edu.jetbrains.plugin.lt.finder.common.{Template, TemplateStatistic}

trait TemplateProcessor {
  def process(treeEncoding: TreeEncoding, occurrenceCount: Int): Template
}

object JavaTemplateProcessor extends TemplateProcessor {

  override def process(treeEncoding: TreeEncoding, occurrenceCount: Int): Template = {
    import DefaultTemplateProcessor._
    val compressedList = getCompressedEncodeList(treeEncoding.encodeList)
    val template = getTemplate(compressedList, occurrenceCount)
    new Template(template.text + getParenthesisList(compressedList).mkString(sep = " "), template.templateStatistic)
  }

  def getParenthesisList(encodeList: List[PathNode]): List[String] = {
    import com.intellij.psi.JavaTokenType._
    def helper(list: List[PathNode], parenthesisList: List[String]): List[String] = list match {
      case x :: xs => x match {
        case EncodeNode(LeafNodeId(LPARENTH, _)) => helper(xs, ")" :: parenthesisList)
        case EncodeNode(LeafNodeId(RPARENTH, _)) => helper(xs, parenthesisList.tail)
        case EncodeNode(LeafNodeId(LBRACE, _)) => helper(xs, "}" :: parenthesisList)
        case EncodeNode(LeafNodeId(RBRACE, _)) => helper(xs, parenthesisList.tail)
        case EncodeNode(LeafNodeId(LBRACKET, _)) => helper(xs, ">" :: parenthesisList)
        case EncodeNode(LeafNodeId(RBRACKET, _)) => helper(xs, parenthesisList.tail)
        case _ => helper(xs, parenthesisList)
      }
      case Nil => parenthesisList
    }

    helper(encodeList, List.empty)
  }
}


object DefaultTemplateProcessor extends TemplateProcessor {

  override def process(treeEncoding: TreeEncoding, occurrenceCount: Int): Template = getTemplate(getCompressedEncodeList(treeEncoding.encodeList), occurrenceCount)

  def getCompressedEncodeList(encodeList: List[PathNode]): List[PathNode] = {
    def helper(encodeList: List[PathNode], prevIsPlaceholder: Boolean, accList: List[PathNode]): List[PathNode] = encodeList match {
      case node :: tail =>
        node match {
          case EncodeNode(leaf: LeafNodeId) =>
            if (leaf.nodeText.matches("(\\s|\\n)*"))
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

  def getTemplate(encodeList: List[PathNode], occurrenceCount: Int): Template = {
    var placeholderCount = 0
    var nodeCount = 0
    val text = encodeList.map {
      case EncodeNode(leaf: LeafNodeId) =>
        nodeCount += 1
        leaf.nodeText
      case Placeholder =>
        placeholderCount += 1
        " #_# "
      case _ => ""
    }.mkString
    new Template(text, new TemplateStatistic(placeholderCount, nodeCount, occurrenceCount))
  }
}