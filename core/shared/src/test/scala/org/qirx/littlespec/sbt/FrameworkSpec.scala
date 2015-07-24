package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.qirx.littlespec.assertion.Assertion
import org.qirx.littlespec.fragments.Result
import org.qirx.littlespec.fragments.Success
import org.qirx.littlespec.fragments.Text

import sbt.testing.Event
import sbt.testing.EventHandler
import sbt.testing.Logger
import sbt.testing.TaskDef
import testUtils.TaskDefFactory
import testUtils.assertion.CollectionAssertions

import scala.scalajs.js.annotation.JSExport

abstract class FrameworkSpec extends Specification with CollectionAssertions {

  def newClassLoader: ClassLoader

  val framework = new TestFramework

  "The framework" - {
    "must be an instance of sbt.testing.Framework" - {
      framework must beAnInstanceOf[sbt.testing.Framework]
    }

    "must have the correct name" - {
      framework.name is "Little Spec"
    }

    "must have org.qirx.littlespec.Specification as a fingerprints" - {
      val fingerprints = framework.fingerprints

      "two fingerprints" - {
        fingerprints.size is 2
      }

      "correct type of fingerprints" - {
        fingerprints.foreach {
          case fingerprint: sbt.testing.SubclassFingerprint =>
            fingerprint.requireNoArgConstructor is true
            fingerprint.superclassName is "org.qirx.littlespec.Specification"
          case fingerprint =>
            failure(fingerprint + "is not an instance of SubclassFingerprint")
        }
        success
      }

      "fingerprint for object" - {
        fingerprints must contain {
          case fingerprint: sbt.testing.SubclassFingerprint =>
            fingerprint.isModule is true
        }
      }

      "fingerprint for class" - {
        fingerprints must contain {
          case fingerprint: sbt.testing.SubclassFingerprint =>
            fingerprint.isModule is false
        }
      }
    }

    "must return the runner correctly" - {
      val args = Array("args")
      val remoteArgs = Array("remoteArgs")
      val testClassLoader = new ClassLoader {}
      val runner = framework.runner(args, remoteArgs, testClassLoader)
      runner must beAnInstanceOf[Runner]
      runner.args is args
      runner.remoteArgs is remoteArgs
      runner.asInstanceOf[Runner].testClassLoader is testClassLoader
    }
  }

  def constructRunnerWithArgs(args: Array[String]): Assertion[Any]

  "The Runner" - {

    def newRunner = framework.runner(Array.empty, Array.empty, newClassLoader)

    "should be able to instantiate a custom reporter" - {
      val reporterName = classOf[ThrowWhenConstructedReporter].getName

      val args = Array("reporter", reporterName)

      framework.runner(args, Array.empty, newClassLoader) must constructRunnerWithArgs(args)
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

  "The Task" - {

    def newTask(testClass: Class[_], forObject: Boolean, args: Array[String] = Array.empty) = {
      val framework = new TestFramework
      val runner = framework.runner(args, Array.empty, newClassLoader)

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

@JSExport
class ThrowWhenConstructedReporter(args: Array[String]) {
  throw ThrowWhenConstructedReporter.Constructed(args)
}
object ThrowWhenConstructedReporter {
  case class Constructed(args: Array[String]) extends Throwable
}

@JSExport
class ThrowingReporter(args:Array[String]) extends SbtReporter {
  def report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]): Unit =
    throw ThrowingReporter.Report(taskDef, eventHandler, loggers, results)
}

object ThrowingReporter {
  case class Report(taskDef: TaskDef, eventHandler: EventHandler, loggers: Seq[Logger], results: Seq[Result]) extends Throwable
}

class EmptyTestSpecification extends Specification
object EmptyTestSpecification extends Specification

class TestSpecification extends Specification {
  "test" - success
}
object TestSpecification extends Specification {
  "test" - success
}