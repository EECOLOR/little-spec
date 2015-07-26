package org.qirx.littlespec.sbt

/**
  * Created by daniel on 7/26/15.
  */
class DefaultSbtReporter(args:Array[String]) extends AbstractDefaultSbtReporter(args) {

   private val ignoredFilesPattern =
     Seq("/org/qirx/littlespec/", "/scala-js/scala-js/v", "/scala/scala/v")
       .mkString("^.*(", "|", ")[^$]*")
       .replaceAll("\\/", "\\\\/")

   override protected def isLittleSpecTest(className: String, fileName: String): Boolean = {
     fileName.contains("/org/qirx/littlespec/") && fileName.endsWith("Spec.scala")
   }

   override protected def isIgnored(className: String, fileName: String): Boolean = {
     fileName matches ignoredFilesPattern
   }
 }