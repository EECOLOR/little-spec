package org.qirx.littlespec.sbt

import org.scalajs.testinterface.TestUtils
import sbt.testing.TaskDef
import sbt.testing.SubclassFingerprint
import org.qirx.littlespec.Specification

class Runner(
  val args: Array[String],
  val remoteArgs: Array[String],
  val testClassLoader: ClassLoader) extends sbt.testing.Runner {

  private val argumentExtractor = new ArgumentExtractor(args)

  private val reporter = argumentExtractor
    .getArg("reporter")
    .map(getReporterWithName(_, args))
    .getOrElse(new DefaultSbtReporter(args))

  private var isDone = false

  def tasks(taskDefs: Array[TaskDef]): Array[sbt.testing.Task] =
    if (isDone) throw new IllegalStateException("Can not call tasks after done is called")
    else taskDefs.map(toTask)

  private def toTask(taskDef: TaskDef): Task[_] = {
    val testInstance =
      getSpecificationWithName(taskDef.fullyQualifiedName, testIsObject(taskDef))

    Task(testInstance, taskDef, reporter)
  }

  private def getSpecificationWithName(name: String, isObject: Boolean): Specification = {
    val testInstance =
      if (isObject) TestUtils.loadModule(name, testClassLoader)
      else TestUtils.newInstance(name, testClassLoader)(Seq())

    testInstance.asInstanceOf[Specification]
  }

  private def getReporterWithName(name: String, args: Array[String]): SbtReporter =
    TestUtils.newInstance(name, testClassLoader)(Seq(args)).asInstanceOf[SbtReporter]

  private def testIsObject(taskDef: TaskDef): Boolean =
    taskDef.fingerprint.asInstanceOf[SubclassFingerprint].isModule

  def done: String = {
    isDone = true
    ""
  }

  // Scala.js test interface specific methods
  def deserializeTask(task: String, deserializer: String => TaskDef): sbt.testing.Task =
    toTask(deserializer(task))

  def serializeTask(task: sbt.testing.Task, serializer: TaskDef => String): String =
    serializer(task.taskDef)

  def receiveMessage(msg: String): Option[String] =
    None // <- ignored
}
