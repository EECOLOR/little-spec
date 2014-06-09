name := "little-spec-sbt"

organization := "org.qirx"

libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0"

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](baseDirectory)

buildInfoPackage := "org.qirx.littlespec.sbt"