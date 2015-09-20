package org.qirx.littlespec.sbt

class DefaultSbtReporter(args:Array[String]) extends AbstractDefaultSbtReporter(args) {

   private val ignoredFilesPattern =
     Seq("/org/qirx/littlespec/", "/scala-js/scala-js/v", "/scala/scala/v")
       .mkString("^.*(", "|", ")[^$]*")
       .replaceAll("\\/", "\\\\/")

   protected def isLittleSpecTest(className: String, fileName: String): Boolean =
     fileName.contains("/org/qirx/littlespec/") && fileName.endsWith("Spec.scala")

   protected def isIgnored(className: String, fileName: String): Boolean =
     fileName matches ignoredFilesPattern
 }
