package org.qirx.littlespec.sbt

import sbt.testing.TaskDef
import sbt.testing.SubclassFingerprint
import org.qirx.littlespec.Specification

class Runner(
  val args: Array[String],
  val remoteArgs: Array[String],
  val testClassLoader: ClassLoader) extends sbt.testing.Runner {

  private val reporter = getArg("reporter")
    .map(testClassLoader.loadClass)
    .map(_.newInstance())
    .map(_.asInstanceOf[SbtReporter])
    .getOrElse(new DefaultSbtReporter)

  private def getArg(name: String) =
    args.sliding(2, 1).find(_.head == name).flatMap(_.lastOption)

  private var isDone = false

  def tasks(taskDefs: Array[TaskDef]): Array[sbt.testing.Task] = {
    if (isDone) throw new IllegalStateException("Can not call tasks after done is called")
    else taskDefs.map(toTask)
  }

  private def toTask(taskDef: TaskDef): Task[_] = {
    val isObject = testIsObject(taskDef)
    val testClass = loadClassWithName(taskDef.fullyQualifiedName, isObject)
    Task(testClass, isObject, taskDef, reporter)
  }

  private def testIsObject(taskDef: TaskDef): Boolean =
    taskDef.fingerprint.asInstanceOf[SubclassFingerprint].isModule

  private def loadClassWithName(name: String, isObject: Boolean): Class[_ <: Specification] = {
    var realName = name
    if (isObject) realName += "$"
    val loadedClass = testClassLoader.loadClass(realName)
    loadedClass.asSubclass(classOf[Specification])
  }

  def done: String = {
    isDone = true
    ""
  }
}