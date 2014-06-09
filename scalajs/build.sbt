import ScalaJSKeys._

name := "little-spec-scalajs"

organization := "org.qirx"

scalaJSSettings

scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"

libraryDependencies ++= Seq(
	"org.scala-lang.modules.scalajs" %% "scalajs-test-bridge" % scalaJSVersion)
