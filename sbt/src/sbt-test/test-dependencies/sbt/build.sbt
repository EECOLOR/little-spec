val libraryVersion = {
  val versionSetting = "library.version"
  val version = System.getProperty(versionSetting)
  if(version == null) throw new RuntimeException(
    s"""|The system property 'versionSetting' is not defined.
        |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  version
}

libraryDependencies += "org.qirx" %% "little-spec" % libraryVersion % "test"

testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework")

unmanagedSourceDirectories in Test += baseDirectory.value / "testClasses"
