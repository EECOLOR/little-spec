package org.qirx.littlespec.scalajs

import scala.scalajs.js
import scala.scalajs.test.Test
import scala.scalajs.test.{TestFramework => ScalaJSTestFramework}
import scala.scalajs.test.TestOutput

object TestFramework extends ScalaJSTestFramework {

  lazy val reporter = new ScalaJsReporter

  def runTest(testOutput: TestOutput, args: js.Array[String])(test: js.Function0[Test]): Unit = {
    val testInstance = test()
    if (testInstance.isInstanceOf[Specification]) {

      val specification = testInstance.asInstanceOf[Specification]
      val results = specification.executeFragments()
      try reporter.report(testOutput, results)
      catch {
        case e: Throwable => testOutput.error("Problem in reporter " + e)
      }

    } else testOutput.error("Can only run tests if they extend " + classOf[Specification].getName)
  }
}