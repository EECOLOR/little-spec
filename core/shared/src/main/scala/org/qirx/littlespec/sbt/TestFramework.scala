package org.qirx.littlespec.sbt

import sbt.testing.SubclassFingerprint

class TestFramework extends sbt.testing.Framework {

  val name: String = "Little Spec"

  val fingerprints: Array[sbt.testing.Fingerprint] = Array(
    new SubclassFingerprint {
      val isModule = true
      val requireNoArgConstructor = true
      val superclassName = "org.qirx.littlespec.Specification"
    },
    new SubclassFingerprint {
      val isModule = false
      val requireNoArgConstructor = true
      val superclassName = "org.qirx.littlespec.Specification"
    })

  def runner(
    arguments: Array[String],
    remoteArguments: Array[String],
    testClassLoader: ClassLoader): sbt.testing.Runner =
    new Runner(arguments, remoteArguments, testClassLoader)

  // Scala.js test interface specific methods
  def slaveRunner(args: Array[String],
                  remoteArgs: Array[String],
                  testClassLoader: ClassLoader,
                  send: String => Unit) =
    runner(args, remoteArgs, testClassLoader)
}
