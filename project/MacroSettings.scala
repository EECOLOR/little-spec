import sbt._
import sbt.Keys._

object MacroSettings {
  
  val settings = Seq(
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
        case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq.empty
        // in Scala 2.10, quasiquotes are provided by macro paradise
        case Some((2, 10)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.0.0" cross CrossVersion.full),
           "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary)
        }
    }
  )
}