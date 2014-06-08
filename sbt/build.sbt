name := "little-spec-sbt"

organization := "org.qirx"

unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value)

unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)

libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0"

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.Framework")

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](baseDirectory)

buildInfoPackage := "org.qirx.littlespec.sbt"