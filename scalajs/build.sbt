import ScalaJSKeys._

name := "little-spec-scalajs"

organization := "org.qirx"

scalaJSSettings

scalaJSTestFramework in Test := "org.qirx.littlespec.scalajs.TestFramework"

libraryDependencies ++= Seq(
	"org.scala-lang.modules.scalajs" %% "scalajs-test-bridge" % scalaJSVersion)

loggingConsole := Some(new TestConsole)

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](
  BuildInfoKey.map(baseDirectory) { case (k, v) => k -> v.toURI}
)

buildInfoPackage := "org.qirx.littlespec.scalajs"
