package org.qirx.littlespec.sbt

import org.qirx.littlespec.assertion.Assertion
import org.scalajs.testinterface.ScalaJSClassLoader

import scala.scalajs.js

object FrameworkSpec extends AbstractFrameworkSpec {
  override def newClassLoader: ClassLoader = new ScalaJSClassLoader(js.Dynamic.global)

  override def constructRunnerWithArgs(args: Array[String]): Assertion[Any] =
    throwA[ThrowWhenConstructedReporter.Constructed].like {
      case ex =>
        ex.args is args
    }
}