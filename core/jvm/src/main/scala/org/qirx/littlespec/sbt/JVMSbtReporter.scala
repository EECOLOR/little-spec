package org.qirx.littlespec.sbt

class DefaultSbtReporter(args:Array[String]) extends AbstractDefaultSbtReporter(args) {

  private val ignoredPackagesPattern =
    Seq("org.qirx.littlespec.", "scala.", "java.", "sbt.")
      .mkString("^(", "|", ")[^$]*")
      .replaceAll("\\.", "\\\\.")

  protected def isLittleSpecTest(className: String, fileName: String): Boolean = {
    className.startsWith("org.qirx.littlespec.") && className.endsWith("Spec")
  }

  protected def isIgnored(className: String, fileName: String): Boolean = {
    className matches ignoredPackagesPattern
  }
}
