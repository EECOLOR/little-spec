package org.qirx.littlespec.sbt

import java.lang.reflect.InvocationTargetException

import org.qirx.littlespec.assertion.Assertion

object JVMFrameworkSpec extends FrameworkSpec {
  override def newClassLoader: ClassLoader = getClass.getClassLoader

  override def constructRunnerWithArgs(args: Array[String]): Assertion[Any] =
    throwA[InvocationTargetException].like {
    case ex =>
      ex.getCause is ThrowWhenConstructedReporter.Constructed(args)
  }
}