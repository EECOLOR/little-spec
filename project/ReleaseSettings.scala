import sbt._
import Keys._
import sbtrelease.ReleasePlugin.{ReleaseKeys => release, releaseSettings}
import sbtrelease.ReleaseStateTransformations._
import org.qirx.sbtrelease.UpdateVersionInFiles.updateVersionInFiles
import PublishSettings.sonatypeReleaseWithInput
import com.typesafe.sbt.SbtPgp.PgpKeys.publishSigned

object ReleaseSettings {
  
  def rootProjectSettings = 
    releaseSettings ++ 
    Seq(
      release.crossBuild := true,
      
      release.releaseProcess := 
        Seq(
          checkSnapshotDependencies,
          inquireVersions,
          runTest,
          setReleaseVersion,
          createReadme,
          updateVersionInReadme,
          commitReleaseVersion,
          tagRelease,
          publishSignedArtifacts,
          closeAndReleaseAtSonatype,
          setNextVersion,
          commitNextVersion,
          pushChanges
        ),
        
      createReadmeTask := {
        streams.value.log.info("Creating README.md")
        val base = baseDirectory.value
        val staticFiles = 
          (base * "*.md").get.filterNot(_.name == "README.md").sorted
        val documentationFiles = 
          (base / "documentation" * "*.md").get.sorted
        val files = staticFiles ++ documentationFiles
        val content = 
          "*This file is generated using sbt*\n\n" +
          files.map(file => IO.read(file)).mkString("\n\n")
        IO.write(readme, content)
      }
    )
  
  lazy val createReadmeTask = taskKey[Unit]("createReadme")

  lazy val createReadme = sbtrelease.releaseTask(createReadmeTask in ThisProject)

  lazy val readme = file("README.md")

  lazy val updateVersionInReadme = updateVersionInFiles(Seq(readme))

  lazy val publishSignedArtifacts = 
    sbtrelease.ReleaseStep(action = publishSignedArtifactsAction, enableCrossBuild = true)

  lazy val publishSignedArtifactsAction = { st: State =>
    val extracted = Project.extract(st)
    val ref = extracted.get(thisProjectRef)
    extracted.runAggregated(publishSigned in Global in ref, st)
  }

  lazy val closeAndReleaseAtSonatype = 
    sbtrelease.releaseTask(sonatypeReleaseWithInput in ThisProject)
}
