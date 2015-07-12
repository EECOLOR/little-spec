package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.scalajs.testinterface.ScalaJSClassLoader
import sbt.testing.{EventHandler, Logger, TaskDef}
import org.qirx.littlespec.fragments.Result
import testUtils.TaskDefFactory
import testUtils.assertion.CollectionAssertions

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

object RunnerSpec extends Specification with CollectionAssertions {

  val framework = new TestFramework

  "The Runner" - {

    val testClassLoader = new ScalaJSClassLoader(js.Dynamic.global)
    def newRunner = framework.runner(Array.empty, Array.empty, testClassLoader)

    "should be able to instantiate a custom reporter" - {
      val reporterName = classOf[ThrowWhenConstructedReporter].getName

      val args = Array("reporter", reporterName)

      framework.runner(args, Array.empty, testClassLoader) must
        throwA[ThrowWhenConstructedReporter.Constructed].like {
          case ex =>
            ex.args is args
        }
    }

    "should return an empty string when done is called" - {
      newRunner.done is ""
    }

    "should throw an illegal state exception when tasks is called after done" - {
      val runner = newRunner
      runner.done
      runner.tasks(Array.empty) must throwAn[IllegalStateException].like { e =>
        e.getMessage contains "done" is true
        e.getMessage contains "tasks" is true
      }
    }

    "should return the correct tasks" - {

      def testTaskCreation(testClassName: String, isObject: Boolean) = {
        var actualTestClassName = testClassName
        if (isObject) actualTestClassName += "$"
        val taskDef = TaskDefFactory.create(testClassName, isObject)

        val tasks = newRunner.tasks(Array(taskDef))

        tasks isLike {
          case Array(task: Task[_]) =>
            task.testInstance.getClass.getName is actualTestClassName
            task.taskDef is taskDef
        }
      }
      "for objects" - testTaskCreation("testUtils.EmptyObject", isObject = true)
      "for classes" - testTaskCreation("testUtils.EmptyClass", isObject = false)
    }
  }
}

@JSExport
class ThrowWhenConstructedReporter(args: Array[String]) extends SbtReporter {
  throw ThrowWhenConstructedReporter.Constructed(args)
  def report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]): Unit =
    throw ThrowingReporter.Report(taskDef, eventHandler, loggers, results)

}
object ThrowWhenConstructedReporter {
  case class Constructed(args: Array[String]) extends Throwable
}