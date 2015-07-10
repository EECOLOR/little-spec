name := "little-spec"

organization := "org.qirx"

scalaVersion := "2.11.7"

crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.7")

PublishSettings.rootProjectSettings

ReleaseSettings.rootProjectSettings

lazy val `little-spec` = project
  .in(file("."))
  .aggregate(`little-spec-core-jvm`, `little-spec-core-js`)

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
    macroSettings ++
    PublishSettings.librarySettings

lazy val `little-spec-core` = crossProject
  .in(file("core"))
  .dependsOn(`little-spec-macros`)
  .settings(
    librarySettings ++ compileTestClassSettings: _*
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    sourceGenerators in Test <+= buildInfo,
    buildInfoKeys := Seq[BuildInfoKey](
      BuildInfoKey.map(baseDirectory) { 
        case (_, value) => "testClasses" -> value / "../shared/testClasses"
      }
    ),
    buildInfoPackage := "org.qirx.littlespec"
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
    testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")
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
