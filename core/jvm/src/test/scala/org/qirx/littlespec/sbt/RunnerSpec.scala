package org.qirx.littlespec.sbt

import java.lang.reflect.InvocationTargetException
import java.net.URLClassLoader

import org.qirx.littlespec.{BuildInfo, Specification}
import testUtils.TaskDefFactory
import testUtils.assertion.CollectionAssertions

object RunnerSpec extends Specification with CollectionAssertions {

  val framework = new TestFramework

  "The Runner" - {

    val location = BuildInfo.testClasses
    val testClassLoader = new URLClassLoader(Array(location.toURI.toURL), getClass.getClassLoader)
    def newRunner = framework.runner(Array.empty, Array.empty, testClassLoader)

    "should be able to instantiate a custom reporter" - {
      val reporterName = classOf[ThrowWhenConstructedReporter].getName

      val args = Array("reporter", reporterName)

      framework.runner(args, Array.empty, testClassLoader) must
        throwA[InvocationTargetException].like {
          case ex => 
            ex.getCause is ThrowWhenConstructedReporter.Constructed(args)
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
      "for objects" - testTaskCreation("test.EmptyObject", isObject = true)
      "for classes" - testTaskCreation("test.EmptyClass", isObject = false)
    }
  }
}

class ThrowWhenConstructedReporter(args: Array[String]) {
  throw ThrowWhenConstructedReporter.Constructed(args)
}
object ThrowWhenConstructedReporter {
  case class Constructed(args: Array[String]) extends Throwable
}