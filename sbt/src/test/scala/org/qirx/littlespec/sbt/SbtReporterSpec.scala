// format: +preserveDanglingCloseParenthesis
package org.qirx.littlespec.sbt

import scala.concurrent.duration.DurationInt
import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Failure
import org.qirx.littlespec.fragments.Fragment
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text
import org.qirx.littlespec.fragments.UnexpectedFailure
import sbt.testing.EventHandler
import sbt.testing.Fingerprint
import sbt.testing.Logger
import sbt.testing.OptionalThrowable
import sbt.testing.Selector
import sbt.testing.Status
import testUtils.TaskDefFactory

class SbtReporterSpec extends Specification {

  val reporter = new DefaultSbtReporter

  "DefaultSbtReporter should" - {

    "report" - {

      "nothing if there are no results" - {
        val (events, logs) = report()

        events is Seq.empty
        logs is Seq.empty
      }

      "the correct information" - {
        val (events, _) = report(successResult(""))

        events is Seq(Event(Status.Success, 1000))
      }

      "success" - {

        val (events, logs) = report(successResult("test"))

        events is Seq(Event(Status.Success, 1000))

        logs is Seq(
          infoLog(s" $successIndicator test"),
          emptyLine)

      }

      "failure" - {
        val (events, logs) = report(failureResult("test"))

        events is Seq(Event(Status.Failure))

        logs is Seq(
          errorLog(s"$failureIndicator test ($fileName:$lineNumber)"),
          errorLog(s"    message"),
          emptyLine)
      }

      "failure with correct location" - {

        val throwable = new Fragment.Failure("failure")
        throwable.setStackTrace(Array(
          new StackTraceElement("org.qirx.littlespec.Class", "abc", "LittleSpecClass", 666),
          new StackTraceElement("scala.Class", "abc", "ScalaClass", 666),
          new StackTraceElement("java.Class", "abc", "JavaClass", 666),
          new StackTraceElement("sbt.Class", "abc", "SbtClass", 666),
          new StackTraceElement("org_qirx_littlespec.Class", "abc", "TestClass", 333)
        ))

        val (_, logs) = report(Failure(Text("test"), "message", throwable))

        logs is Seq(
          errorLog(s"$failureIndicator test (TestClass:333)"),
          errorLog(s"    message"),
          emptyLine)
      }

      "unexpected failure" - {
        val unexpectedFailure = new Exception("message")
        val stackTrace = unexpectedFailure.getStackTrace.take(2).map { s =>
          "SbtReporterSpec.scala:" + s.getLineNumber + " (org.qirx.littlespec.sbt.SbtReporterSpec)"
        }
        val (events, logs) = report(UnexpectedFailure(Text("test"), unexpectedFailure))

        events is Seq(Event(Status.Error))

        logs is Seq(
          errorLog(s"$failureIndicator test"),
          errorLog(s"    Exception: message"),
          errorLog(s"    - ${stackTrace(0)}"),
          errorLog(s"    - ${stackTrace(1)}"),
          "trace" -> "[suppressed]",
          emptyLine
        )
      }

      "unexpected failure with correct stack trace" - {
        val littlespec = new StackTraceElement("org.qirx.littlespec.Class", "abc", "LittleSpecClass", 666)
        val sbt = new StackTraceElement("sbt.Class", "abc", "SbtClass", 666)
        val java = new StackTraceElement("java.Class", "abc", "JavaClass", 666)
        val scala = new StackTraceElement("scala.Class", "abc", "ScalaClass", 666)

        val unexpectedFailure = new Exception("message")
        unexpectedFailure.setStackTrace(Array(
          littlespec, sbt, java, scala,
          new StackTraceElement("org_qirx_littlespec.Class1", "abc", "TestClass1", 111),
          littlespec, sbt, java, scala,
          new StackTraceElement("org_qirx_littlespec.Class2", "abc", "TestClass2", 222),
          littlespec, sbt, java, scala
        ))

        val (events, logs) = report(UnexpectedFailure(Text("test"), unexpectedFailure))

        logs is Seq(
          errorLog(s"$failureIndicator test"),
          errorLog(s"    Exception: message"),
          errorLog(s"    - TestClass1:111 (org_qirx_littlespec.Class1)"),
          errorLog(s"    - TestClass2:222 (org_qirx_littlespec.Class2)"),
          "trace" -> "[suppressed]",
          emptyLine
        )
      }

      "unexpected failure with cause" - {
        def line(s: StackTraceElement) =
          "SbtReporterSpec.scala:" + s.getLineNumber + " (org.qirx.littlespec.sbt.SbtReporterSpec)"

        val unexpectedCause = new Exception("cause")
        val stackTrace2 = unexpectedCause.getStackTrace.take(2).map(line)
        val unexpectedFailure = new Exception("failure", unexpectedCause)
        val stackTrace1 = unexpectedFailure.getStackTrace.take(2).map(line)

        val (events, logs) = report(UnexpectedFailure(Text("test"), unexpectedFailure))

        logs is Seq(
          errorLog(s"$failureIndicator test"),
          errorLog(s"    Exception: failure"),
          errorLog(s"    - ${stackTrace1(0)}"),
          errorLog(s"    - ${stackTrace1(1)}"),
          errorLog(s"    == Caused by =="),
          errorLog(s"    Exception: cause"),
          errorLog(s"    - ${stackTrace2(0)}"),
          errorLog(s"    - ${stackTrace2(1)}"),
          "trace" -> "[suppressed]",
          emptyLine
        )
      }

      "pending" - {
        val (events, logs) = report(Pending(Text("test"), "message"))

        events is Seq(Event(Status.Pending))

        val coloredMessage = warnColor + "message" + resetColor

        logs is Seq(
          "warn" -> s" $pendingIndicator test - $coloredMessage",
          emptyLine)
      }

      "nested" - {
        val (events, logs) = report(CompoundResult(Text("test"), Seq.empty))

        events is Seq.empty

        logs is Seq(
          infoLog(" test"),
          emptyLine)
      }

      "nested with indentation" - {
        val (_, logs) = report(CompoundResult(Text("outer"), Seq(successResult("inner"))))

        logs is Seq(
          infoLog(s" outer"),
          infoLog(s"   $successIndicator inner"),
          emptyLine)
      }

      "nested with extra nesting" - {
        val (_, logs) = report(
          CompoundResult(Text("outer"), Seq(
            CompoundResult(Text("inner"), Seq(
              successResult("inner"),
              failureResult("inner"))))))

        logs is Seq(
          infoLog(s" outer"),
          infoLog(s"   - inner"),
          infoLog(s"     $successIndicator inner"),
          errorLog(s"    $failureIndicator inner ($fileName:$lineNumber)"),
          errorLog(s"        message"),
          emptyLine)
      }

      "multiline correctly for different systems" - {
        val (_, logs) = report(CompoundResult(Text("outer"),
          Seq(successResult("inner1\ninner2\r\ninner3\rinner4"))))

        logs is Seq(
          infoLog(s" outer"),
          infoLog(s"   $successIndicator inner1\n     inner2\n     inner3\n     inner4"),
          emptyLine)
      }

      "example failure" - {
        val example = Code(
          """|val `1` = 1
             |val `2` = 2
             |`1` is `2`""".stripMargin)

        val (_, logs) = report(Failure(example, "message", throwableFailure))

        logs is Seq(
          errorLog(s"$failureIndicator Example failed ($fileName:$lineNumber)"),
          errorLog(s"  ${ example.text.replaceAll("\n", "\n  ")}"),
          errorLog(s"    message"),
          emptyLine)
      }
    }

    "strip ansi color codes if the logger does not support it" - {
      val out = new HandlerAndLogger(ansiCodesSupported = false)
      reporter.report(taskDef, out, Array(out), Seq(successResult("test")))
      out.logs is Seq(
        infoLog(" + test"),
        emptyLine)
    }
  }

  val errorColor = "\u001b[31m"
  val successColor = "\u001b[32m"
  val infoColor = "\u001b[36m"
  val warnColor = "\u001b[33m"

  val resetColor = "\u001b[0m"

  val pendingIndicator = warnColor + "o" + resetColor
  val successIndicator = successColor + "+" + resetColor
  val failureIndicator = errorColor + "X" + resetColor

  def successResult(message: String) = Success(Text(message))(1.second)
  def failureResult(message: String) = Failure(Text(message), "message", throwableFailure)

  val throwableFailure = new Fragment.Failure("failure")
  val (fileName, lineNumber) = {
    val s = throwableFailure.getStackTrace.head
    (s.getFileName, s.getLineNumber)
  }

  def errorLog(message: String) =
    "error" -> message

  def infoLog(message: String) =
    "info" -> message

  def report(in: Result*): (Seq[Event], Seq[(String, String)]) = {
    val out = new HandlerAndLogger
    reporter.report(taskDef, out, Array(out), in)
    (out.events, out.logs)
  }

  val emptyLine = infoLog("")

  val taskDef = TaskDefFactory.create("test")

  case class Event(
    status: Status,
    duration: Long,
    fullyQualifiedName: String,
    fingerprint: Fingerprint,
    selector: Selector,
    throwable: OptionalThrowable)

  object Event {
    def apply(event: sbt.testing.Event): Event =
      Event(event.status, event.duration, event.fullyQualifiedName, event.fingerprint, event.selector, event.throwable)

    def apply(status: Status, duration: Long = 0, throwable: OptionalThrowable = new OptionalThrowable): Event =
      apply(status, duration, taskDef.fullyQualifiedName, taskDef.fingerprint, taskDef.selectors.head, throwable)
  }

  class HandlerAndLogger(val ansiCodesSupported: Boolean = true) extends EventHandler with Logger {
    var events = Seq.empty[Event]
    var logs = Seq.empty[(String, String)]

    def handle(event: sbt.testing.Event): Unit =
      events :+= Event(event)

    def debug(message: String): Unit = logs :+= "debug" -> message
    def error(message: String): Unit = logs :+= "error" -> message
    def info(message: String): Unit = logs :+= "info" -> message
    def warn(message: String): Unit = logs :+= "warn" -> message
    def trace(message: Throwable): Unit = logs :+= "trace" -> "[suppressed]"
  }
}