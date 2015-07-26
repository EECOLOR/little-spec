package org.qirx.littlespec.sbt

/**
 * Created by daniel on 7/26/15.
 */
class DefaultSbtReporter(args:Array[String]) extends AbstractDefaultSbtReporter(args) {

  private val ignoredPackagesPattern =
    Seq("org.qirx.littlespec.", "scala.", "java.", "sbt.")
      .mkString("^(", "|", ")[^$]*")
      .replaceAll("\\.", "\\\\.")

  override protected def isLittleSpecTest(className: String, fileName: String): Boolean = {
    className.startsWith("org.qirx.littlespec.") && className.endsWith("Spec")
  }

  override protected def isIgnored(className: String, fileName: String): Boolean = {
    className matches ignoredPackagesPattern
  }
}
