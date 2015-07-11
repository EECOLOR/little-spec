package org.qirx.littlespec.sbt

import org.qirx.littlespec.Specification
import org.qirx.littlespec.fragments.Result
import sbt.testing.EventHandler
import sbt.testing.Logger
import sbt.testing.TaskDef
import testUtils.assertion.CollectionAssertions

object FrameworkSpec extends Specification with CollectionAssertions {

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
}

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