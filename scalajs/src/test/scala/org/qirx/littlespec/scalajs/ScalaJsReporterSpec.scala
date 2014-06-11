package org.qirx.littlespec.scalajs

import scala.scalajs.test.TestOutput
import scala.scalajs.test.TestOutputLog
import org.qirx.littlespec.macros.Location
import org.qirx.littlespec.io.Source
import org.qirx.littlespec.Specification

object ScalaJsReporterSpec extends Specification { self =>

  class Example(implicit location: Location) { self =>
    def expecting(result: self.type => FragmentBody) =
      createFragment(Source.codeAtLocation(location), result(self))
  }

  val scalaJsReporter = new ScalaJsReporter

  "ScalaJsReporter should report the results correctly" - {
    val specification = new Specification {
      "Testing output" - {
        "nested" - {
          "success" - {
            success
          }
          "failure" - {
            failure("this is a failure")
          }
          "unexpected" - {
            sys.error("this is an unexpected failure")
          }
          "todo" - {
            todo
          }
          "pending" - {
            pending("pending")
          }
          example {
            1 + 2 is 3
          }
          example {
            1 + 2 is 2
          }
          "nested deeper" - {
            "empty" - ()
          }
        }
      }
      "Testing output\nmultiline" - {
        "nested\nmultiline" - {
          "success\nmultiline" - {
            success
          }
          "failure\nmultiline" - {
            failure("this is a failure\nmultiline")
          }
          "unexpected\nmultiline" - {
            sys.error("this is an unexpected failure\nmultiline")
          }
          "todo\nmultiline" - {
            todo
          }
          "pending\nmultiline" - {
            pending("pending\nmultiline")
          }
          example {
            val x = 1 + 2
            val y = 3
            x is y
          }
          example {
            val x = 1 + 2
            val y = 2
            x is y
          }
          "nested deeper\nmultiline" - {
            "empty\nmultiline" - ()
          }
        }
      }
    }

    val testOutput = new TestOutputMock
    scalaJsReporter.report(testOutput, specification.executeFragments())
    val result = testOutput.result
    val expected =
      s"""[$info]  Testing output
[$info]    - nested
[$info]      $S success
[$error]     $F failure (org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:21)
[$error]         this is a failure
[$error]     $F unexpected
[$error]         this is an unexpected failure
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:26
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:15
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:40
[$error]         Generated test launcher file:1
[$info]      $P todo - TODO
[$info]      $P pending - pending
[$info]      $S 1 + 2 is 3
[$error]     $F 1 + 2 is 2 (org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:14)
[$error]         3 is not equal to 2
[$info]      - nested deeper
[$info]        $P empty - TODO
[$info]  Testing output
[$info]  multiline
[$info]    - nested
[$info]      multiline
[$info]      $S success
[$info]        multiline
[$error]     $F failure
[$error]       multiline (org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:21)
[$error]         this is a failure
[$error]         multiline
[$error]     $F unexpected
[$error]       multiline
[$error]         this is an unexpected failure
[$error]         multiline
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:26
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:15
[$error]         org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:40
[$error]         Generated test launcher file:1
[$info]      $P todo
[$info]        multiline - TODO
[$info]      $P pending
[$info]        multiline - pending
[$info]        multiline
[$info]      $S val x = 1 + 2
[$info]        val y = 3
[$info]        x is y
[$error]     $F val x = 1 + 2
[$error]       val y = 2
[$error]       x is y (org/qirx/littlespec/scalajs/ScalaJsReporterSpec.scala:16)
[$error]         3 is not equal to 2
[$info]      - nested deeper
[$info]        multiline
[$info]        $P empty
[$info]          multiline - TODO
[$info]${" "}
"""

    /*
    if (result != expected) {
      println("result: ")
      println(result.split("\n").mkString("", "|\n", "|"))
      println("=========: ")
      println("expected: ")
      println(expected.split("\n").mkString("", "|\n", "|"))
      println("=========: ")
    } else {
      println("Result (for visual inspection):")
      println(result.split("\n").mkString(">  ", "\n>  ", ""))
    }
    */

    result is expected
  }.disabled(
      "Disabled while source mappings are disabled, see: https://github.com/scala-js/scala-js/issues/727")

  class TestOutputMock extends TestOutput {
    var result = ""
    type Color = String

    val infoColor: Color = ""
    val errorColor = "\u001b[31m"
    val successColor = "\u001b[32m"
    val warningColor = "\u001b[33m"
    val resetColor = "\u001b[0m"

    def color(message: String, color: Color): String =
      color + message + resetColor

    def error(message: String, stack: Array[StackTraceElement]): Unit = {}
    def error(message: String): Unit =
      result += s"[${self.error}] $message\n"
    def failure(message: String, stack: Array[StackTraceElement]): Unit = {}
    def failure(message: String): Unit =
      result += s"[${self.error}] $message\n"
    def succeeded(message: String): Unit =
      result += s"[${self.info}] $message\n"
    def skipped(message: String): Unit = {}
    def pending(message: String): Unit =
      result += s"[${self.info}] $message\n"
    def ignored(message: String): Unit = {}
    def canceled(message: String): Unit = {}

    val log: TestOutputLog = new TestOutputLog {
      def info(message: String): Unit =
        result += s"[${self.info}] $message\n"
      def warn(message: String): Unit = {}
      def error(message: String): Unit =
        result += s"[${self.error}] $message\n"
    }
  }

  lazy val testOutput = new TestOutputMock
  import testOutput._

  lazy val info = "info"
  lazy val S = color("+", successColor)
  lazy val F = color("X", errorColor)
  lazy val P = color("o", warningColor)
  lazy val error = color("error", errorColor)
}