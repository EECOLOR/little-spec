name := "little-spec"

organization := "org.qirx"

val directorySettings = Seq(
  unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
)

lazy val `little-spec-macros` = project
  .in( file("macros") )
  .settings(directorySettings:_*)

lazy val `little-spec-core` = project
  .in( file("core") )
  .dependsOn(`little-spec-macros`)
  .settings(directorySettings:_*)

lazy val `little-spec-sbt` = project
  .in( file("sbt") )
  .dependsOn(`little-spec-core`)
  .settings(directorySettings:_*)
  .settings(
    // compile test classes before running test in little spec sbt
    test in Test <<= (test in Test)
      .dependsOn(compile in Compile in `little-spec-sbt-test-classes`) 
  )

lazy val `little-spec-scalajs` = project
  .in( file("scalajs") )
  //.dependsOn(`little-spec-core`)
  //.dependsOn(`little-spec-macros`)
  .settings(directorySettings:_*)
  .settings(
    // depend on sources in order to give scalajs a chance to compile them
    addSourceDirectoriesOf(`little-spec-core`),
    addSourceDirectoriesOf(`little-spec-macros`))
  .settings(MacroSettings.settings: _*)
  
lazy val `little-spec-sbt-test-classes` = project
  .in( file("sbt/testClasses") )
  .dependsOn(`little-spec-core`)  
 
// compile sbt runner before running tests in little-spec
test in Test in `little-spec-core` <<= (test in Test in `little-spec-core`)
  .dependsOn(compile in Compile in `little-spec-sbt`) 

// add compiled class of little-spec-sbt to test classpath of little-spec-core
unmanagedClasspath in Test in `little-spec-core` += (classDirectory in Compile in `little-spec-sbt`).value

def addSourceDirectoriesOf(project:Project) = 
  unmanagedSourceDirectories in Compile ++= (unmanagedSourceDirectories in Compile in project).value