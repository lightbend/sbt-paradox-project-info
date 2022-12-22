import sbt.Keys.{homepage, scmInfo}
enablePlugins(ParadoxPlugin)
paradoxTheme := None

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
    version            := "0.1.2.3",
    crossScalaVersions := Seq("2.12.17")
  )

lazy val testkit = project
  .settings(commonSettings)
  .settings(
    version := "4.3.2.1"
  )
