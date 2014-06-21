name := "little-spec"

organization := "org.qirx"

scalaVersion := "2.11.1"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.1")

PublishSettings.rootProjectSettings

ReleaseSettings.rootProjectSettings

lazy val `little-spec` = project
  .in( file(".") )
  .settings(
    clean <<= clean.dependsOn(
      clean in `little-spec-macros`,
      clean in `little-spec-sbt`,
      clean in `little-spec-scalajs`,
      clean in `little-spec-extra-documentation`
    )
  )
  .aggregate(`little-spec-sbt`, `little-spec-scalajs`, `little-spec-extra-documentation`)

lazy val librarySettings = 
  onlyScalaSources ++ 
  Seq(
    name := "little-spec",
    organization := "org.qirx",
    core(Compile, "main"),
    core(Test, "test"),
    resolvers += Classpaths.typesafeReleases
  ) ++ 
  scriptedSettings ++
  Seq(
    scriptedLaunchOpts ++= Seq(
      "-Dlibrary.version=" + version.value,
      "-Dscala.version=" + scalaVersion.value
    )
  ) ++
  macrosOutputAsResource ++
  macrosAsDependency ++ 
  PublishSettings.librarySettings

lazy val `little-spec-sbt` = project
  .in( file("sbt") )
  .settings(
    librarySettings ++ buildInfoSettings ++ compileTestClassSettings:_*)
  .settings(
    libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
    testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework"),
    testOptions += Tests.Argument("reporter", "org.qirx.littlespec.reporter.MarkdownReporter"),
    testOptions += Tests.Argument("documentationTarget", 
      ((baseDirectory in ThisBuild).value / "documentation").getAbsolutePath))
  .settings(
    sourceGenerators in Test <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.map(baseDirectory) { 
        case (_, value) => "testClasses" -> value / "testClasses" 
      }
    ),
    buildInfoPackage := "org.qirx.littlespec"
  )
  .settings(internalDependencyClasspath in Test ++= (internalDependencyClasspath in LateInitialization).value)
  
lazy val `little-spec-scalajs` = project
  .in( file("scalajs") )
  .settings(
    librarySettings ++ scalaJSSettings:_*)
  .settings(
    // make sure the scalajs files from resourceGenerators (macros) are included
    includeFilter in resourceGenerators in Compile := {
      import scala.scalajs.tools.jsdep.JSDependencyManifest.ManifestFileName
      val includedFiles = (includeFilter in resourceGenerators in Compile).value
      includedFiles || ManifestFileName || "*.sjsir" || "*.js"
    })
  .settings(
    libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-test-bridge" % scalaJSVersion,
    ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"
  )

// separate project to help with IDE support
lazy val `little-spec-macros` = project 
  .in( file("macros") )
  .settings(onlyScalaSources ++ macroSettings ++ scalaJSSettings:_*)
  .settings(publishArtifact := false)  
  
def extraLibrarySettings(libraryName:String) = 
  onlyScalaSources ++ 
  Seq(
    name := libraryName,
    organization := "org.qirx",
    resolvers += Classpaths.typesafeReleases
  ) ++ 
  scriptedSettings ++
  Seq(
    scriptedLaunchOpts ++= Seq(
      "-Dlibrary.version=" + version.value,
      "-Dscala.version=" + scalaVersion.value
    )
  ) ++
  PublishSettings.librarySettings
  
lazy val `little-spec-extra-documentation` = project
  .in( file("extra/documentation") )
  .settings(extraLibrarySettings("little-spec-extra-documentation"):_*)
  .dependsOn(`little-spec-sbt`)

lazy val LateInitialization = config("lateInitialization")
  
internalDependencyClasspath in LateInitialization in `little-spec-sbt` := Seq(
  Attributed.blank(
    (classDirectory in Compile in `little-spec-extra-documentation`)
      .map(identity) // convert the setting into a task
      .dependsOn(compile in Compile in `little-spec-extra-documentation`)
      .value
  )
)

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

lazy val macrosAsDependency = 
  macroSettings ++
  Seq(
    internalDependencyClasspath in Compile += macrosCompiledClassDirectory.value,
    internalDependencyClasspath in Test += macrosCompiledClassDirectory.value
  )

lazy val macrosOutputAsResource = Seq(
  includeFilter in resourceGenerators in Compile := "*.class",
  resourceGenerators in Compile += Def.task {
    val dir = macrosCompiledClassDirectory.value
    val includedFiles = (includeFilter in resourceGenerators in Compile).value
    val files = (dir ** includedFiles).get
    files
  }.taskValue,
  managedResourceDirectories in Compile += macrosClassDirectory.value
)
  
lazy val macrosClassDirectory = classDirectory in Compile in `little-spec-macros`

lazy val macrosCompiledClassDirectory = 
   macrosClassDirectory
     .map(identity) // convert the setting into a task
     .dependsOn(compile in Compile in `little-spec-macros`)
