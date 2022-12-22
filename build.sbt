ThisBuild / scalaVersion := "2.12.15"

sbtPlugin := true

enablePlugins(SbtPlugin)
import scala.collection.JavaConverters._
scriptedLaunchOpts += ("-Dproject.version=" + version.value)
scriptedLaunchOpts ++= java.lang.management.ManagementFactory.getRuntimeMXBean.getInputArguments.asScala.filter(a =>
  Seq("-Xmx", "-Xms", "-XX", "-Dfile").exists(a.startsWith)
)

crossSbtVersions := List("1.1.0")
organization     := "com.lightbend.paradox"
name             := "sbt-paradox-project-info"

addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.4.3")

libraryDependencies ++= Seq(
  "com.typesafe"   % "config"    % "1.4.2",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test // ApacheV2
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
homepage := Some(url("https://github.com/lightbend/sbt-paradox-project-info"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/lightbend/sbt-paradox-project-info"),
    "git@github.com:lightbend/sbt-paradox-project-info.git"
  )
)
developers += Developer(
  "contributors",
  "Contributors",
  "https://gitter.im/lightbend/paradox",
  url("https://github.com/lightbend/sbt-paradox-project-info/graphs/contributors")
)
organizationName := "Lightbend Inc."
startYear        := Some(2018)

enablePlugins(AutomateHeaderPlugin)

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / test / publishArtifact := false
ThisBuild / pomIncludeRepository   := (_ => false)
sonatypeProfileName                := "com.lightbend"

ThisBuild / githubWorkflowJavaVersions := List(
  JavaSpec.temurin("8"),
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17")
)

ThisBuild / githubWorkflowTargetBranches := Seq("main")
ThisBuild / githubWorkflowBuild          := Seq(WorkflowStep.Sbt(List("test", "scripted")))
