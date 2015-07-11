package org.qirx.littlespec.sbt

import sbt.testing.EventHandler
import sbt.testing.Logger
import org.qirx.littlespec.Specification
import sbt.testing.{Task => BaseTask, _}

case class Task[T <: Specification](
  testInstance: Specification,
  taskDef: sbt.testing.TaskDef,
  reporter: SbtReporter) extends sbt.testing.Task {

  val tags: Array[String] = Array.empty

  def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[sbt.testing.Task] = {

    val results = testInstance.executeFragments()

    reporter.report(taskDef, eventHandler, loggers, results)

    Array.empty
  }

  def execute(eventHandler: EventHandler, loggers: Array[Logger],
              continuation: (Array[BaseTask]) => Unit): Unit = {
    continuation(execute(eventHandler, loggers))
  }
}