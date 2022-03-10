import sbt.Keys.{homepage, scmInfo}

enablePlugins(ParadoxPlugin)
paradoxTheme := None

version in ThisBuild := "0.1.2.3"
projectInfoVersion   := "snapshot"

val commonSettings = Seq(
  organization := "com.lightbend",
  licenses     := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
  homepage     := Some(url("https://github.com/lightbend/sbt-paradox-project-info")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lightbend/sbt-paradox-project-info"),
      "git@github.com:lightbend/sbt-paradox-project-info.git"
    )
  )
)

lazy val core = Project(id = "coreId", base = file("core"))
  .settings(commonSettings)
  .settings(
    crossScalaVersions := Seq("2.12.7", "2.11.12")
  )
