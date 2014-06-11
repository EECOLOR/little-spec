import sbt._
import Keys._
import com.typesafe.sbt.SbtPgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.SonatypeKeys.sonatypeRelease
import xerial.sbt.Sonatype.sonatypeSettings

object PublishSettings {

  def rootProjectSettings = 
    disablePublishing ++
    Seq(
      crossScalaVersions := Seq("2.10.4", "2.11.1")
    )
    
  lazy val sonatypeReleaseWithInput = taskKey[Boolean]("sonatypeReleaseWithInput")

  lazy val disablePublishing = Seq(
    publishArtifact := false,
    publishSigned := (),
    publish := ()
  )

  lazy val librarySettings = 
    sonatypeSettings ++
    Seq(
      sonatypeReleaseWithInput <<= sonatypeRelease.toTask(" Release"),
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <url>https://github.com/eecolor/little-spec</url>
        <licenses>
          <license>
            <name>The MIT License (MIT)</name>
            <url>https://github.com/EECOLOR/little-spec/blob/master/LICENSE</url>
          </license>
        </licenses>
        <scm>
          <url>git@github.com:EECOLOR/little-spec.git</url>
          <connection>scm:git:git@github.com:EECOLOR/little-spec.git</connection>
        </scm>
        <developers>
          <developer>
            <id>EECOLOR</id>
            <name>Erik Westra</name>
          </developer>
        </developers>
      )
    )
}
