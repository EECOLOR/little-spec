organization := "org.qirx"

scalaVersion in ThisBuild := "2.11.7"

crossScalaVersions in ThisBuild := Seq("2.10.5", "2.11.7")

PublishSettings.rootProjectSettings

ReleaseSettings.rootProjectSettings

lazy val `little-spec` = project
  .in(file("."))
  .settings(name := "little-spec-root")
  .aggregate(`little-spec-core-jvm`, `little-spec-core-js`, `little-spec-extra-documentation`)

lazy val librarySettings =
  Seq(
    name := "little-spec",
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

lazy val `little-spec-core` = crossProject
  .in(file("core"))
  .dependsOn(`little-spec-macros`)
  .settings(
    librarySettings ++ macroSettings: _*
  )
  .jsSettings(
    libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion,
    testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scala-sbt" % "test-interface" % "1.0",
      "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
    ),
    testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework"),
    testOptions += Tests.Argument("reporter", "org.qirx.littlespec.reporter.MarkdownReporter"),
    testOptions += Tests.Argument("documentationTarget",
      ((baseDirectory in ThisBuild).value / "documentation").getAbsolutePath),
    internalDependencyClasspath in Test ++= (internalDependencyClasspath in LateInitialization).value
  )

lazy val `little-spec-core-js` = `little-spec-core`.js
lazy val `little-spec-core-jvm` = `little-spec-core`.jvm

// separate project to help with IDE support
lazy val `little-spec-macros` = crossProject
  .in(file("macros"))
  .settings(macroSettings: _*)
  .settings(publishArtifact := false)
  .jsSettings()
  .jvmSettings()

lazy val `little-spec-macros-js` = `little-spec-macros`.js
lazy val `little-spec-macros-jvm` = `little-spec-macros`.jvm

lazy val macroSettings =
  Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies ++= {
      if (scalaVersion.value startsWith "2.11.") Seq.empty
      else Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.1" cross CrossVersion.binary
      )
    }
  )

// documentation markdown reporter
lazy val `little-spec-extra-documentation` = project
  .in(file("extra/documentation"))
  .settings(extraLibrarySettings("little-spec-extra-documentation"): _*)
  .dependsOn(`little-spec-core-jvm`)

lazy val LateInitialization = config("lateInitialization")

internalDependencyClasspath in LateInitialization in `little-spec-core-jvm` := Seq(
  Attributed.blank(
    (classDirectory in Compile in `little-spec-extra-documentation`)
      .map(identity) // convert the setting into a task
      .dependsOn(compile in Compile in `little-spec-extra-documentation`)
      .value
  )
)

def extraLibrarySettings(libraryName: String) =
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
