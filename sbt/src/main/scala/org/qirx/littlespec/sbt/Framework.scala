package org.qirx.littlespec.sbt

import sbt.testing.SubclassFingerprint

class Framework extends sbt.testing.Framework {

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

}