Provides a simple markdown reporter. It picks up specifications that are 
in the `documentation` package.

Add the following settings to your `build.sbt` file:

```scala
libraryDependencies += "org.qirx" %% "little-spec-extra-documentation" % "version" % "test"
)

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")

testOptions += Tests.Argument("reporter", "org.qirx.littlespec.reporter.MarkdownReporter")

testOptions += Tests.Argument("documentationTarget", 
  ((baseDirectory in ThisBuild).value / "documentation").getAbsolutePath)
```
