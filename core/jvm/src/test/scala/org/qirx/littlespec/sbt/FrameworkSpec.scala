package org.qirx.littlespec.sbt

import java.lang.reflect.InvocationTargetException

import org.qirx.littlespec.assertion.Assertion

object FrameworkSpec extends AbstractFrameworkSpec {
  def newClassLoader: ClassLoader = getClass.getClassLoader

  def constructRunnerWithArgs(args: Array[String]): Assertion[Any] =
    throwA[InvocationTargetException].like {
    case ex =>
      ex.getCause is ThrowWhenConstructedReporter.Constructed(args)
  }
}
