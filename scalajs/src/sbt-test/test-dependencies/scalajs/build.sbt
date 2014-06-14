scalaJSSettings

val libraryVersion = {
  val versionSetting = "library.version"
  val version = System.getProperty(versionSetting)
  if(version == null) throw new RuntimeException(
    s"""|The system property 'versionSetting' is not defined.
        |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  version
}

libraryDependencies += "org.qirx" %%% "little-spec" % libraryVersion % "test"

ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"

unmanagedSourceDirectories in Test += baseDirectory.value / "testClasses"
