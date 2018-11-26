# sbt-paradox-project-info [![bintray-badge][]][bintray] [![travis-badge][]][travis]

A [paradox](https://github.com/lightbend/paradox/) directive that includes standardised project information in the generated documentation.

## Usage

Add the Paradox plug-in as sbt plug-on

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-project-info" % "<latest>")
```

Create a project info file in `project/project-info.conf` using HOCON format:

```hocon
project-info {
  scala-versions: ["2.12", "2.13"]
  jdk-versions: ["OpenJDK 8"]
  core {
    title: "The core project"
    scala-versions: ${project-info.scala-versions}
    jdk-versions: ${project-info.jdk-versions}
    jpms-name: "alpakka.core"
    issues: {
      url: "https://github.com/lightbend/sbt-paradox-project-info/issues"
      text: "Github issues"
    }
    release-notes: {
      url: "https://github.com/lightbend/sbt-paradox-project-info/releases"
      text: "Github releases"
    }
    levels: [
      {
        readiness: Supported
        since: "2018-11-22"
        since-version: "0.22"
      }
      {
        readiness: Incubating
        since: "2018-08-22"
        since-version: "0.16"
        note: "Community rocks this module"
      }
    ]
  }
}
```
[Full Source](https://github.com/lightbend/sbt-paradox-project-info/blob/master/src/sbt-test/project-info/happy-path/project/project-info.conf)

Use the `@@project-info` directive in the Paradox markdown files and reference the project

```markdown
# The Test Kit

@@project-info { project="core" }

The quick brown fox...

```
[Example](https://github.com/lightbend/sbt-paradox-project-info/blob/master/src/sbt-test/project-info/happy-path/src/main/paradox/index.md)

## License

The license is Apache 2.0, see LICENSE.

## Maintenance notes

**This project is NOT supported under the Lightbend subscription.**

Feel free to ping contributors for code review or discussions. Pull requests are very welcome–thanks in advance!

[bintray]:               https://bintray.com/sbt/sbt-plugin-releases/sbt-paradox-project-info
[bintray-badge]:         https://api.bintray.com/packages/sbt/sbt-plugin-releases/sbt-paradox-project-info/images/download.svg
[travis]:                https://travis-ci.com/lightbend/sbt-paradox-project-info
[travis-badge]:          https://travis-ci.com/lightbend/sbt-paradox-project-info.svg?branch=master

