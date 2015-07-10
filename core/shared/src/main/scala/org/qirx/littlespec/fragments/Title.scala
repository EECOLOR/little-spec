package org.qirx.littlespec.fragments

sealed trait Title {
  def text: String
}
object Title {
  def unapply(title:Title) = Option(title).map(_.text)
}
case class Text(text: String) extends Title
case class Code(text: String) extends Title
