package org.qirx.littlespec.sbt

import java.lang.reflect.InvocationTargetException

import org.qirx.littlespec.Specification
import org.qirx.littlespec.assertion.Assertion
import testUtils.assertion.CollectionAssertions

object JVMFrameworkSpec extends FrameworkSpec with Specification with CollectionAssertions {
  override def newClassLoader: ClassLoader = getClass.getClassLoader

  override def constructRunnerWithArgs(args: Array[String]): Assertion[Any] =
    throwA[InvocationTargetException].like {
    case ex =>
      ex.getCause is ThrowWhenConstructedReporter.Constructed(args)
  }
}