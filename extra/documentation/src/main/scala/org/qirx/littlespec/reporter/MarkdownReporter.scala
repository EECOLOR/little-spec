package org.qirx.littlespec.reporter

import java.io.File

import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.fragments.Pending
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Failure
import org.qirx.littlespec.fragments.UnexpectedFailure
import org.qirx.littlespec.fragments.CompoundResult
import org.qirx.littlespec.fragments.Text
import org.qirx.littlespec.fragments.Code
import org.qirx.littlespec.fragments.Title

import org.qirx.littlespec.sbt.ArgumentExtractor
import org.qirx.littlespec.sbt.DefaultSbtReporter
import org.qirx.littlespec.sbt.SbtReporter

import sbt.testing.EventHandler
import sbt.testing.Logger
import sbt.testing.TaskDef

class MarkdownReporter(args:Array[String]) extends SbtReporter {

  val argumentExtractor = new ArgumentExtractor(args)
  
  val defaultReporter = new DefaultSbtReporter(args)

  lazy val targetDirectory = {{
      val directory = argumentExtractor
          .getArg("documentationTarget")
          .map(new File(_))
          .getOrElse(sys.error("Test option `documentationTarget` not found"))
      if (!directory.exists) directory.mkdir
      directory
    }
  }

  def report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]): Unit = {

    val fullyQualifiedName = taskDef.fullyQualifiedName
    if (fullyQualifiedName startsWith "documentation.") {

      val fileName = MarkdownReporter.fileName(fullyQualifiedName)
      val file = new File(targetDirectory, fileName)

      println(s"Reporting $fullyQualifiedName in ${file.getAbsolutePath}")

      val text =
        s"""|**This documentation is generated from `$fullyQualifiedName`**
            |
            |---
            |${toMarkdown(results)}""".stripMargin

      printToFile(file, _ println text)
    }

    defaultReporter.report(taskDef, eventHandler, loggers, results)
  }

  def toMarkdown(results: Seq[Result]): String =
    results.map {
      case Pending(title, message) =>
        s"""|${toMarkdown(title)}
            |> Pending: $message
            |""".stripMargin

      case Success(title) =>
        toMarkdown(title)

      case UnexpectedFailure(title, throwable) =>
        s"""|${toMarkdown(title)}
            |> Unexpected failure: $throwable
            |""".stripMargin

      case Failure(title, message, failure) =>
        s"""|${toMarkdown(title)}
            |> Failure: $message
            |""".stripMargin

      case CompoundResult(title, results) =>
        s"""|${toMarkdown(title)}
            |${toMarkdown(results)}""".stripMargin

    }.mkString("\n")

  def toMarkdown(title: Title): String =
    title match {
      case Text(text) => text
      case Code(code) =>
        s"""|```scala
            |$code
            |```""".stripMargin
    }

  def printToFile(f: java.io.File, op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }
}

object MarkdownReporter {
  def fileName(fullyQualifiedName:String) = 
    name(fullyQualifiedName) + ".md"
  def name(fullyQualifiedName:String) =
    fullyQualifiedName.replaceAll("documentation\\.", "")
}