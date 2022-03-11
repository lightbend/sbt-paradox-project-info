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
  "com.typesafe"   % "config"    % "1.3.3",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test // ApacheV2
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

// no API docs
Compile / doc / sources                := Seq.empty
Compile / packageDoc / publishArtifact := false

enablePlugins(AutomateHeaderPlugin)
scalafmtOnCompile := true

// Disable publish for now
ThisBuild / githubWorkflowPublishTargetBranches := Seq()

ThisBuild / githubWorkflowJavaVersions := List(
  JavaSpec.temurin("8"),
  JavaSpec.temurin("11"),
  JavaSpec.temurin("17")
)

ThisBuild / githubWorkflowTargetBranches := Seq("master")
