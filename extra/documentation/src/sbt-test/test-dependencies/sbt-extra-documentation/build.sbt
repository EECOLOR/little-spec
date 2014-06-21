scalaVersion := getProperty("scala.version")

libraryDependencies ++= Seq(
  "org.qirx" %% "little-spec" % getProperty("library.version") % "test",
  "org.qirx" %% "little-spec-extra-documentation" % getProperty("library.version") % "test"
)

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")

testOptions += Tests.Argument("reporter", "org.qirx.littlespec.reporter.MarkdownReporter")

testOptions += Tests.Argument("documentationTarget", 
  ((baseDirectory in ThisBuild).value / "documentation").getAbsolutePath)

unmanagedSourceDirectories in Test += baseDirectory.value / "testClasses"

def getProperty(propertyName:String) =
  Option(System.getProperty(propertyName)) getOrElse {
    throw new RuntimeException(
      s"""|The system property '$propertyName' is not defined.
          |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  }
  
val checkDocumentationContent = taskKey[Unit]("checkDocumentationContent")

checkDocumentationContent := {
  val content = IO.read(file("documentation/Test.md"))
  val expected = 
    """|#Title
  	   |test
  	   |example
  	   |```scala
  	   |1 + 1 is 2
  	   |```""".stripMargin
  if (!content.containsSlice(expected)) {
    println("expected: \n" + expected)
    println("content: \n" + content)
    error("Incorrect content generated")
  }
}
  