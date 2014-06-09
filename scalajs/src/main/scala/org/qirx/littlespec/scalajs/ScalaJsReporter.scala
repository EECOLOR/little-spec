package org.qirx.littlespec.scalajs

import scala.scalajs.test.TestOutput

import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Failure
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.UnexpectedFailure

class ScalaJsReporter {

  def report(out: TestOutput, results: Seq[Result]): Unit = {

    val info = logInfo(out)
    val error = logError(out)

    def report(results: Seq[Result], level: Int): Unit =
      results.foreach {
        case CompoundResult(title, results) =>
          val titleText = title.text
          if (level == 0)
            info(titleText, noIndicator, level)
          else
            info(titleText, compoundIndicator, level)

          report(results, level + 1)

        case Success(title) =>
          reportSuccess(out)(title.text, successIndicator, level)

        case Failure(title, message, failure) =>
          val titleText = title.text + getLocationOf(failure)
          reportFailure(out)(titleText, failureIndicator, level)
          error(message, noIndicator, level + 2)

        case UnexpectedFailure(title, throwable) =>
          reportError(out)(title.text, failureIndicator, level)
          error(throwable.getMessage, noIndicator, level + 2)
          filteredStackTrace(throwable).foreach { s =>
            error(getLocationOf(s), noIndicator, level + 2)
          }

        case Pending(title, message) =>
          val titleText = title.text + " - " + message
          reportPending(out)(titleText, pendingIndicator, level)
      }

    report(results, 0)
    out.log.info("")
  }

  private val errorColor = "\u001b[31m"
  private val successColor = "\u001b[32m"
  private val warningColor = "\u001b[33m"
  private val resetColor = "\u001b[0m"

  private val noIndicator = ""
  private val successIndicator = successColor + "+" + resetColor
  private val pendingIndicator = warningColor + "o" + resetColor
  private val failureIndicator = errorColor + "X" + resetColor
  private val compoundIndicator = "-"

  private val reportError = reportMessage(_.error, _.log.error, true) _
  private val reportFailure = reportMessage(_.failure, _.log.error, true) _
  private val reportPending = reportMessage(_.pending, _.log.info, false) _
  private val reportSuccess = reportMessage(_.succeeded, _.log.info, false) _
  private val logInfo = reportMessage(_.log.info, _.log.info, false) _
  private val logError = reportMessage(_.log.error, _.log.error, true) _

  private type Method = TestOutput => (String => Unit)

  private def reportMessage(first: Method, second: Method, error: Boolean)(out: TestOutput)(message: String, indicator: String, level: Int) = {
    val indentation = ("  " * level) + (if (error) "" else " ")
    val (actualIndicator, spacing) =
      if (indicator.length > 0) (indicator + " ", "  ")
      else ("", "")

    val split = message.split("\n")

    val firstLine = indentation + actualIndicator + split.head
    first(out)(firstLine)

    val otherLines = split.tail.map(indentation + spacing + _)
    otherLines.foreach(second(out))
  }

  private def filteredStackTrace(throwable: Throwable) =
    throwable.getStackTrace.filter { s =>
      val className = guessedClassName(s)
      isLittleSpecTest(className) || !(className matches pattern)
    }

  private def getLocationOf(s: StackTraceElement): String =
    guessedFileName(s) + ":" + s.getLineNumber

  private def getLocationOf(throwable: Throwable): String =
    filteredStackTrace(throwable).headOption.map { s =>
      " (" + getLocationOf(s) + ")"
    }.getOrElse("")

  private def isLittleSpecTest(className: String) =
    className.startsWith("org.qirx.littlespec.") && className.endsWith("Spec")

  private val ignoredPackages = Seq("org.qirx.littlespec.", "scala.", "java.", "sbt.")
  private val pattern =
    ignoredPackages
      .mkString("^(", "|", ")[^$]*")
      .replaceAll("\\.", "\\\\.")

  private def guessedFileName(s: StackTraceElement) = {
    val parts = s.getFileName.split("src/(((main|test)/(scala|java)|library)/)?")
    parts match {
      case Array(_, classFile) => classFile
      case Array(other) => other
    }
  }

  private def guessedClassName(s: StackTraceElement) = {
    val fileName = guessedFileName(s)
    val className = fileName.replace(".scala", "").replaceAll("/|\\\\", ".")
    className.split("\\$").head
  }
}