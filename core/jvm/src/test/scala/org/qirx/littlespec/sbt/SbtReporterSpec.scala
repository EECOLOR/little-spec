package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.{UnexpectedFailure, Text, Failure, Fragment}

object SbtReporterSpec extends AbstractSbtReporterSpec with Specification {

  override def newDefaultReporter(args:Array[String]): SbtReporter = {
    new DefaultSbtReporter(Array.empty)
  }

  override def containStackTraceOf(exception: Exception): Assertion[Seq[(String, String)]] = {
    new Assertion[Seq[(String, String)]] {
      override def assert(logs: => Seq[(String, String)]) = {
        val stackTrace = exception.getStackTrace.filter(s => s.getFileName.contains("SbtReporterSpec")).take(2).map { s =>
          s"$fileName:" + s.getLineNumber + " (org.qirx.littlespec.sbt.AbstractSbtReporterSpec)"
        }

        Right(logs.take(2) is Seq(
          errorLog(s"    - ${stackTrace(0)}"),
          errorLog(s"    - ${stackTrace(1)}")
        ))
      }
    }
  }

  "DefaultSbtReporter on JVM should" - {

    "report" - {

      "failure with correct location and ignore little-spec and common packages" - {

        val throwable = new Fragment.Failure("failure")
        throwable.setStackTrace(Array(
          new StackTraceElement("org.qirx.littlespec.Class", "abc", "LittleSpecClass", 666),
          new StackTraceElement("scala.Class", "abc", "ScalaClass", 666),
          new StackTraceElement("java.Class", "abc", "JavaClass", 666),
          new StackTraceElement("sbt.Class", "abc", "SbtClass", 666),
          new StackTraceElement("com.example.Class", "abc", "TestClass", 333)
        ))

        val (_, logs) = report(Failure(Text("test"), "message", throwable))

        logs is Seq(
          errorLog(s"$failureIndicator test (TestClass:333)"),
          errorLog(s"    message"),
          emptyLine)
      }

      "unexpected failure with correct stack trace and ignore little-spec and common packages" - {
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

        val (_, logs) = report(UnexpectedFailure(Text("test"), unexpectedFailure))

        logs is Seq(
          errorLog(s"$failureIndicator test"),
          errorLog(s"    Exception: message"),
          errorLog(s"    - TestClass1:111 (org_qirx_littlespec.Class1)"),
          errorLog(s"    - TestClass2:222 (org_qirx_littlespec.Class2)"),
          "trace" -> "[suppressed]",
          emptyLine
        )
      }
    }
  }

}