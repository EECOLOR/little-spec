name := "little-spec"

organization := "org.qirx"

lazy val `little-spec-macros` = project
  .in( file("macros") )

lazy val `little-spec-core` = project
  .in( file("core") )
  .dependsOn(`little-spec-macros`)

lazy val `little-spec-sbt` = project
  .in( file("sbt") )
  .dependsOn(`little-spec-core`)

lazy val `little-spec-scalajs` = project
  .in( file("scalajs") )
  .dependsOn(`little-spec-core`)  
  
lazy val `little-spec-sbt-test-classes` = project
  .in( file("sbt/testClasses") )
  .dependsOn(`little-spec-core`)  
 
// compile test classes before running test in little spec sbt
test in Test in `little-spec-sbt` <<= 
  (test in Test in `little-spec-sbt`).dependsOn(compile in Compile in `little-spec-sbt-test-classes`)  

// tests for little-spec-core are run using little-spec-sbt
unmanagedClasspath in Test in `little-spec-core` += (classDirectory in Compile in `little-spec-sbt`).value