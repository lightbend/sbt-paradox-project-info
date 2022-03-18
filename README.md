# sbt-paradox-project-info [![maven-central-badge][]][maven-central][![github-actions-badge][]][github-actions]

A [paradox](https://github.com/lightbend/paradox/) directive that includes standardised project information in the generated documentation.

## Usage

Add the Paradox plug-in as sbt plug-on

```scala
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-project-info" % <latest>)
```

Create a project info file in `project/project-info.conf` using HOCON format:

```hocon
project-info {
  # version is overridden from the `projectInfoVersion` key (which defaults to sbt's project version)
  version: "current"
  scala-versions: ["2.12", "2.13"]
  jdk-versions: ["OpenJDK 8"]
  core {
    title: "The core project"
    // if undefined, sbt's crossScalaVersions are used
    scala-versions: ${project-info.scala-versions}
    jdk-versions: ${project-info.jdk-versions}
    jpms-name: "alpakka.core"
    snapshots: {
      text: "Snapshots are available"
      url: "snapshots.html"
      new-tab: false
    }
    issues: {
      url: "https://github.com/lightbend/sbt-paradox-project-info/issues"
      text: "Github issues"
    }
    release-notes: {
      url: "https://github.com/lightbend/sbt-paradox-project-info/releases"
      text: "Github releases"
    }
    api-docs: [
      {
        text: "Scaladoc"
        url: "https://developer.lightbend.com/docs/api/alpakka/"${project-info.version}"/akka/stream/alpakka/index.html"
      }
    ]
    forums: [
      {
        text: "Lightbend Discuss"
        url: "https://discuss.lightbend.com/c/akka/"
      }
      {
        text: "akka/alpakka-kafka Gitter channel"
        url: "https://gitter.im/akka/alpakka-kafka"
      }
    ]
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

Use the `@@project-info` directive in the Paradox markdown files and reference the project by its sbt `projectId` (when using the `project` sbt macro it is the name of the `val`).

```markdown
# The Test Kit

@@project-info { projectId="core" }

The quick brown fox...

```
[Example](https://github.com/lightbend/sbt-paradox-project-info/blob/master/src/sbt-test/project-info/happy-path/src/main/paradox/index.md)

## License

The license is Apache 2.0, see LICENSE.

## Maintenance notes

**This project is NOT supported under the Lightbend subscription.**

Feel free to ping contributors for code review or discussions. Pull requests are very welcome–thanks in advance!

[maven-central]:         https://maven-badges.herokuapp.com/maven-central/com.lightbend.paradox/sbt-paradox-project-info
[maven-central-badge]:   https://maven-badges.herokuapp.com/maven-central/com.lightbend.paradox/sbt-paradox-project-info/badge.svg
[github-actions]:        https://github.com/lightbend/sbt-paradox-project-info/actions/workflows/ci.yml?query=branch%3Amaster
[github-actions-badge]:  https://github.com/lightbend/sbt-paradox-project-info/actions/workflows/ci.yml/badge.svg?branch=master
