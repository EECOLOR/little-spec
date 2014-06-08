package org.qirx.littlespec.io

import scala.annotation.tailrec
import scala.io.{Source => ScalaSource}
import org.qirx.littlespec.macros.Location
import org.qirx.littlespec.fragments.Code

object Source {
  def codeAtLocation(location: Location) = {

    val Location(file, line, column) = location
    val lines = ScalaSource.fromFile(file).getLines
    val contentAtLine = lines.drop(line - 1).mkString("\n")
    val contentAtStartPosition = contentAtLine.substring(column - 1)

    Code(getContent(contentAtStartPosition))
  }

  private def getContent(chars: String): String = {

    import utils._

    @tailrec def getContent(
      chars: List[Char],
      open: Int = 0,
      indentation: Option[Int] = None,
      acc: Vector[Char] = Vector.empty): String = {

      chars match {
        case Nil => sys.error("Unexpected end of string encountered")

        // at start
        case '{' :: t if open == 0 =>
          getContent(skipWhiteSpace(t), open = 1)

        // eol, indentation known
        case '\n' :: t if (indentation.isDefined) =>
          getContent(skipIndentation(t, indentation), open, indentation, acc :+ '\n')

        // eol, detect indentation
        case '\n' :: t =>
          val newIndentation = detectIndentation(t)
          getContent(skipIndentation(t, newIndentation), open, newIndentation, acc)

        // single line comment
        case '/' :: '/' :: t =>
          val (comment, rest) = splitAtEndOfLine(t)
          getContent(rest, open, indentation, acc ++ "//" ++ comment)

        // multiline comment
        case '/' :: '*' :: t =>
          val (comment, rest) = extractMultilineComment(t, indentation)
          getContent(rest, open, indentation, acc ++ "/*" ++ comment ++ "*/")

        // at end
        case '}' :: t if open == 1 => getResult(acc)

        case '{' :: t => getContent(t, open + 1, indentation, acc :+ '{')
        case '}' :: t => getContent(t, open - 1, indentation, acc :+ '}')
        case x :: t => getContent(t, open, indentation, acc :+ x)
      }
    }
    getContent(chars.toList)
  }

  private[this] object utils {

    def isWhiteSpace(c: Char) =
      c == ' ' || c == '\t'

    def detectIndentation(t: List[Char]) =
      Some(t.takeWhile(isWhiteSpace).size)

    def skipIndentation(t: List[Char], indentation: Option[Int]) = {
      val (possibleWaste, rest) = t.splitAt(indentation.getOrElse(0))
      possibleWaste.dropWhile(isWhiteSpace) ++ rest
    }

    def splitAtEndOfLine(t: List[Char]) =
      t.span(_ != '\n')

    def extractMultilineComment(t: List[Char], indentation: Option[Int]) = {

      def splitAtEndOfComment = {
        val commentBody = t.sliding(2, 1).takeWhile(_ != Seq('*', '/'))
        val (comment, rest) = t.splitAt(commentBody.size)
        (comment, rest.drop(2))
      }

      val (comment, rest) = splitAtEndOfComment

      val commentWithoutIndentation = {
        val waste = "\n" + (" " * indentation.getOrElse(0))
        comment.mkString.replaceAll(waste, "\n")
      }

      (commentWithoutIndentation, rest)
    }

    def skipWhiteSpace(t: List[Char]) =
      t.dropWhile(_ == ' ')

    def getResult(acc: Vector[Char]) =
      acc.lastOption match {
        // at end single line or multiline
        case Some('\n' | ' ') => acc.dropRight(1).mkString
        case _ => acc.mkString
      }
  }
}