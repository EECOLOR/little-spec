name := "little-spec"

organization := "org.qirx"

scalaVersion := "2.11.1"

PublishSettings.rootProjectSettings

ReleaseSettings.rootProjectSettings

lazy val `little-spec` = project
  .in( file(".") )
  .settings(
    clean <<= clean.dependsOn(
      clean in `little-spec-macros`,
      clean in `little-spec-sbt`,
      clean in `little-spec-scalajs`
    )
  )
  .aggregate(`little-spec-sbt`, `little-spec-scalajs`)

lazy val librarySettings = 
  Seq(
    name := "little-spec",
    organization := "org.qirx",
    core(Compile, "main"),
    core(Test, "test"),
    macrosOutputAsResource
  ) ++ 
  macrosAsDependency ++ 
  PublishSettings.librarySettings

lazy val `little-spec-sbt` = project
  .in( file("sbt") )
  .settings(
    onlyScalaSources ++ librarySettings ++ buildInfoSettings ++ compileTestClassSettings:_*)
  .settings(
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
    testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework"),
    testOptions += Tests.Argument("reporter", "documentation.reporter.MarkdownReporter"))
  .settings(
    sourceGenerators in Compile <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.map(baseDirectory in ThisBuild) { 
        case (_, value) => "documentationTarget" -> value / "documentation" 
      },
      BuildInfoKey.map(baseDirectory) { 
        case (_, value) => "testClasses" -> value / "testClasses" 
      }
    ),
    buildInfoPackage := "org.qirx.littlespec"
  )

lazy val `little-spec-scalajs` = project
  .in( file("scalajs") )
  .settings(
    onlyScalaSources ++ librarySettings ++ scalaJSSettings:_*)
  .settings(
    libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-test-bridge" % scalaJSVersion,
    ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"
  )

// separate project to help with IDE support
lazy val `little-spec-macros` = project 
  .in( file("macros") )
  .settings(onlyScalaSources ++ macroSettings ++ scalaJSSettings:_*)
  .settings(publishArtifact := false)  
  
lazy val macroSettings = 
  Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies ++= {
      if (scalaVersion.value startsWith "2.11.") Seq.empty
      else Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary
      )
    }  
  )
  
def core(configuration:Configuration, config:String) = 
  unmanagedSourceDirectories in configuration +=
    (baseDirectory in ThisBuild).value / "core" / "src" / config / "scala"  
  
lazy val macrosFullClasspath = fullClasspath in Compile in `little-spec-macros`
  
lazy val macrosOutputAsResource = 
  unmanagedResourceDirectories in Compile += 
    (classDirectory in Compile in `little-spec-macros`).value

lazy val macrosAsDependency = Seq(
  internalDependencyClasspath in Compile ++= macrosFullClasspath.value,
  internalDependencyClasspath in Test ++= macrosFullClasspath.value
)
  
lazy val onlyScalaSources = Seq(
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
)

lazy val CompileTestClasses = config("compileTestClasses").extend(Compile)

lazy val compileTestClassSettings = 
  inConfig(CompileTestClasses)(Defaults.configSettings) ++
  Seq(
    unmanagedSourceDirectories in CompileTestClasses := Seq(baseDirectory.value / "testClasses"),
    classDirectory in CompileTestClasses := baseDirectory.value / "testClasses",
    internalDependencyClasspath in CompileTestClasses ++= (fullClasspath in Compile).value,
    // compile test classes before running tests
    test in Test <<= (test in Test).dependsOn(compile in CompileTestClasses)
  )

