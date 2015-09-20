package org.qirx.littlespec.sbt

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Failure
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text
import org.qirx.littlespec.fragments.Title
import org.qirx.littlespec.fragments.UnexpectedFailure

import sbt.testing.Event
import sbt.testing.EventHandler
import sbt.testing.Logger
import sbt.testing.OptionalThrowable
import sbt.testing.Status
import sbt.testing.TaskDef

trait SbtReporter {
  def report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]): Unit
}

abstract class AbstractDefaultSbtReporter(args:Array[String]) extends SbtReporter {

  protected def isLittleSpecTest(className: String, fileName: String): Boolean

  protected def isIgnored(className: String, fileName: String): Boolean

  def report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]): Unit = {

    val event = eventFor(taskDef, eventHandler) _
    val logLevel = logStringFor(loggers) _

    def report(results: Seq[Result], level: Int): Unit = {
      val log = logLevel(level, true)
      val logError = logLevel(level, false)

      results.foreach {
        case CompoundResult(Title(title), results) =>
          val indicator = if (level == 0) noIndicator else compoundIndicator
          log(_.info, title, indicator)
          report(results, level + 1)

        case s @ Success(Title(title)) =>
          event(Status.Success, s.duration)
          log(_.info, title, successIndicator)

        case UnexpectedFailure(Title(title), throwable) =>
          val logExceptionLine = logLevel(level + 2, false)(_.error, _: String, noIndicator)

          event(Status.Error, Duration.Zero)
          logError(_.error, title, failureIndicator)
          logException(throwable, logExceptionLine)
          logFor(loggers)(_.trace, throwable)

        case Failure(Text(title), message, failure) =>
          val location = getLocationOf(failure)

          event(Status.Failure, Duration.Zero)
          logError(_.error, title + location, failureIndicator)
          logLevel(level + 2, false)(_.error, message, noIndicator)

        case Failure(Code(example), message, failure) =>
          val location = getLocationOf(failure)

          event(Status.Failure, Duration.Zero)
          logError(_.error, "Example failed" + location, failureIndicator)
          logLevel(level + 1, false)(_.error, example, noIndicator)
          logLevel(level + 2, false)(_.error, message, noIndicator)

        case Pending(Title(title), message) =>
          event(Status.Pending, Duration.Zero)
          val coloredMessage = warningColor + message + resetColor
          log(_.warn, title + " - " + coloredMessage, pendingIndicator)
      }
    }

    report(results, 0)
    if (results.nonEmpty) logEmptyLine(loggers)
  }

  private val errorColor = "\u001b[31m"
  private val successColor = "\u001b[32m"
  private val warningColor = "\u001b[33m"
  private val resetColor = "\u001b[0m"

  private val noIndicator = None
  private val successIndicator = Some(successColor + "+" + resetColor)
  private val pendingIndicator = Some(warningColor + "o" + resetColor)
  private val failureIndicator = Some(errorColor + "X" + resetColor)
  private val compoundIndicator = Some("-")

  private def logEmptyLine(loggers: Seq[Logger]) =
    logStringFor(loggers)(level = 0, extraSpace = false)(_.info, "", noIndicator)

  private def eventFor(taskDef: TaskDef, eventHandler: EventHandler)(actualStatus: Status, actualDuration: FiniteDuration) =
    eventHandler.handle(
      new Event {
        val duration = actualDuration.toMillis
        val fingerprint = taskDef.fingerprint
        val fullyQualifiedName = taskDef.fullyQualifiedName
        val selector = taskDef.selectors.head
        val status: Status = actualStatus
        val throwable = new OptionalThrowable
      })

  private def logStringFor(loggers: Seq[Logger])(level: Int, extraSpace: Boolean)(method: Logger => String => Unit, message: String, indicator: Option[String]) = {
    val (indicatorWithSeparator, indicatorIndentation) =
      indicator.map(_ + " " -> "  ").getOrElse("" -> "")
    val levelIndentation = "  " * level
    val compensation = if (extraSpace) " " else ""
    val levelMessage =
      message
        .split("(\r\n|\r|\n)")
        .mkString(
          start = levelIndentation + compensation + indicatorWithSeparator,
          sep = "\n" + levelIndentation + compensation + indicatorIndentation,
          end = "")

    logFor(loggers, removeColorFrom)(method, levelMessage)
  }

  private def logFor[T](loggers: Seq[Logger], colorRemover: T => T = identity[T] _)(
    method: Logger => T => Unit, message: T) =

    loggers.foreach { logger =>

      val cleanMessage =
        if (logger.ansiCodesSupported) message
        else colorRemover(message)

      method(logger)(cleanMessage)
    }

  private def removeColorFrom(message: String) = {
    val colorPattern = raw"\u001b\[\d{1,2}m"
    message.replaceAll(colorPattern, "")
  }

  def logException(throwable: Throwable, log: String => Unit):Unit = {
    log(throwable.getClass.getSimpleName + ": " + throwable.getMessage)

    filteredStackTrace(throwable)
      .map { s => s"- ${s.getFileName}:${s.getLineNumber} (${classNameOf(s)})" }
      .distinct
      .foreach(log)

    Option(throwable.getCause).foreach { x =>
      log("== Caused by ==")
      logException(x, log)
    }
  }

  private def filteredStackTrace(throwable: Throwable) =
    throwable.getStackTrace.filter { s =>
      val className = classNameOf(s)
      isLittleSpecTest(className, s.getFileName) || !isIgnored(className, s.getFileName)
    }

  private def getLocationOf(throwable: Throwable) =
    filteredStackTrace(throwable).headOption.map { s =>
      " (" + s.getFileName + ":" + s.getLineNumber + ")"
    }.getOrElse("")

  private def classNameOf(s: StackTraceElement) =
    s.getClassName.split("\\$").head
}