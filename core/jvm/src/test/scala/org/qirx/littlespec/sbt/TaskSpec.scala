package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.{Success, Text}
import sbt.testing.{Event, EventHandler, Logger}
import testUtils.TaskDefFactory
import testUtils.assertion.CollectionAssertions

object TaskSpec extends Specification with CollectionAssertions {

  "The Task" - {

    def newTask(testClass: Class[_], forObject: Boolean, args: Array[String] = Array.empty) = {
      val framework = new TestFramework
      val runner = framework.runner(args, Array.empty, getClass.getClassLoader)

      val taskDef = TaskDefFactory.create(testClass.getName, forObject)

      runner.tasks(Array(taskDef)).head
    }

    val noOpEventHandler =
      new EventHandler {
        def handle(e: Event): Unit = ???
      }

    val noOpLogger =
      new Logger {
        def ansiCodesSupported(): Boolean = ???

        def debug(message: String): Unit = ???

        def error(message: String): Unit = ???

        def info(message: String): Unit = ???

        def trace(throwable: Throwable): Unit = ???

        def warn(message: String): Unit = ???
      }

    "should return no tasks for an empty specification" - {

      def testExecuteEmpty(forObject: Boolean) = {
        val task = newTask(classOf[EmptyTestSpecification], forObject)
        val tasks = task.execute(noOpEventHandler, Array(noOpLogger))

        tasks.size is 0
      }

      "for classes" - testExecuteEmpty(forObject = false)
      "for objects" - testExecuteEmpty(forObject = true)
    }

    "should report the results correctly" - {

      def testReporting(forObject: Boolean) = {

        val className = classOf[ThrowingReporter].getName

        val task = newTask(
          testClass = classOf[TestSpecification],
          forObject,
          args = Array("reporter", className))

        task.execute(noOpEventHandler, Array(noOpLogger)) must
          throwA[ThrowingReporter.Report].like {
            case ThrowingReporter.Report(taskDef, eventHandler, Seq(logger), results) =>
              taskDef is task.taskDef
              eventHandler is noOpEventHandler
              logger is noOpLogger
              results isLike {
                case Seq(Success(Text("test"))) => success
              }
          }
      }

      "for classes" - testReporting(forObject = false)
      "for objects" - testReporting(forObject = true)
    }
  }
}