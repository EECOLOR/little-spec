name := "little-spec-core"

organization := "org.qirx"

libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0"

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")

testOptions += Tests.Argument("reporter", "documentation.reporter.MarkdownReporter")

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](baseDirectory)

buildInfoPackage := "org.qirx.littlespec"
