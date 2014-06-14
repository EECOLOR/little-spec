scalaJSSettings

scalaVersion := getProperty("scala.version")

libraryDependencies += "org.qirx" %%% "little-spec" % getProperty("library.version") % "test"

ScalaJSKeys.scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"

unmanagedSourceDirectories in Test += baseDirectory.value / "testClasses"

def getProperty(propertyName:String) =
  Option(System.getProperty(propertyName)) getOrElse {
    throw new RuntimeException(
      s"""|The system property '$propertyName' is not defined.
          |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  }