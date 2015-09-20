package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.{UnexpectedFailure, Text, Failure, Fragment}

object SbtReporterSpec extends AbstractSbtReporterSpec with Specification {

  def newDefaultReporter(args:Array[String]): SbtReporter = {
    new DefaultSbtReporter(Array.empty)
  }

  def containStackTraceOf(exception: Exception): Assertion[Seq[(String, String)]] = {
    new Assertion[Seq[(String, String)]] {
      def assert(logs: => Seq[(String, String)]) = {
        val stackTrace = exception.getStackTrace.filter(s => s.getFileName.contains("SbtReporterSpec")).take(2).map { s =>
          s"$fileName:" + s.getLineNumber + " (<jscode>)"
        }

        Right(logs.take(2) is Seq(
          errorLog(s"    - ${stackTrace(0)}"),
          errorLog(s"    - ${stackTrace(1)}")
        ))
      }
    }
  }

  "DefaultSbtReporter on JS should" - {

    "report" - {

      "failure with correct location and ignore little-spec and common packages" - {

        val throwable = new Fragment.Failure("failure")
        throwable.setStackTrace(Array(
          new StackTraceElement("<jscode>", "abc", "file:/some/path/org/qirx/littlespec/Class.scala", 666),
          new StackTraceElement("<jscode>", "abc", "https://some/url/scala/scala/vX.X.X/Class.scala", 666),
          new StackTraceElement("<jscode>", "abc", "https://some/url/scala-js/scala-js/vX.X.X/Class.scala", 666),
          new StackTraceElement("<jscode>", "abc", "file:/some/path/com/example/TestClass.scala", 333)
        ))

        val (_, logs) = report(Failure(Text("test"), "message", throwable))

        logs is Seq(
          errorLog(s"$failureIndicator test (file:/some/path/com/example/TestClass.scala:333)"),
          errorLog(s"    message"),
          emptyLine)
      }
    }

    "unexpected failure with correct stack trace and ignore little-spec and common packages" - {
      val littlespec = new StackTraceElement("<jscode>", "abc", "file:/some/path/org/qirx/littlespec/Class.scala", 666)
      val scalajs = new StackTraceElement("<jscode>", "abc", "https://some/url/scala-js/scala-js/vX.X.X/Class.scala", 666)
      val scala = new StackTraceElement("<jscode>", "abc", "https://some/url/scala-js/scala-js/vX.X.X/Class.scala", 666)

      val unexpectedFailure = new Exception("message")
      unexpectedFailure.setStackTrace(Array(
        littlespec, scalajs, scala,
        new StackTraceElement("<jscode>", "abc", "file:/some/path/com/example/TestClass1.scala", 111),
        littlespec, scalajs, scala,
        new StackTraceElement("<jscode>", "abc", "file:/some/path/com/example/TestClass2.scala", 222),
        littlespec, scalajs, scala
      ))

      val (_, logs) = report(UnexpectedFailure(Text("test"), unexpectedFailure))

      logs is Seq(
        errorLog(s"$failureIndicator test"),
        errorLog(s"    Exception: message"),
        errorLog(s"    - file:/some/path/com/example/TestClass1.scala:111 (<jscode>)"),
        errorLog(s"    - file:/some/path/com/example/TestClass2.scala:222 (<jscode>)"),
        "trace" -> "[suppressed]",
        emptyLine
      )
    }
  }

}
