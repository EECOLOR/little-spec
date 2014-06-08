name := "little-spec-macros"

organization := "org.qirx"

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value)

unmanagedSourceDirectories in Test := Seq((scalaSource in Test).value)
