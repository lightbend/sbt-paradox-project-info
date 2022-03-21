/*
 * Copyright 2018 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lightbend.paradox.projectinfo

import java.time.LocalDate
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.collection.immutable

class ProjectInfoSpec extends AnyWordSpec with Matchers {

  "Level" should {
    "read from config" in {
      val in =
        """
          |level {
          |  readiness: Incubating
          |  since: "2018-11-22"
          |  since-version: "0.12"
          |}
        """.stripMargin
      val c = ConfigFactory.parseString(in).getConfig("level")
      Level(c, SampleReadinessLevels.values) should be(
        Level(SampleReadinessLevels.Incubating, LocalDate.of(2018, 11, 22), "0.12", None, None)
      )
    }
  }

  "ProjectInfo" should {
    "read from config" in {
      val in =
        """
          |core {
          |  scala-versions: ["2.12", "2.13"]
          |  jdk-versions: ["OpenJDK 8"]
          |  jpms-name: "alpakka.core"
          |}
        """.stripMargin
      val c = ConfigFactory.parseString(in).getConfig("core")
      ProjectInfo("core", SampleReadinessLevels.values, c) should be(
        ProjectInfo(
          "core",
          "core",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          Some("alpakka.core"),
          issues = None,
          apiDocs = immutable.Seq.empty,
          forums = immutable.Seq.empty,
          releaseNotes = None,
          snapshots = None,
          levels = List()
        )
      )
    }
  }

  "Sub-project" should {
    "read from config" in {
      val in =
        """
          |project-info {
          |  core: {
          |    title: "The core project"
          |    scala-versions: ["2.12", "2.13"]
          |    jdk-versions: ["OpenJDK 8"]
          |    jpms-name: "alpakka.core"
          |    levels: [
          |      {
          |        readiness: Incubating
          |        since: "2018-11-22"
          |        since-version: "0.18"
          |        note: "Community rocks this module"
          |      }
          |    ]
          |  }
          |}
        """.stripMargin
      val c    = ConfigFactory.parseString(in).getConfig("project-info")
      val name = "core"
      val conf = c.getConfig(name)
      ProjectInfo(name, SampleReadinessLevels.values, conf) should be(
        ProjectInfo(
          "core",
          title = "The core project",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          Some("alpakka.core"),
          issues = None,
          apiDocs = immutable.Seq.empty,
          forums = immutable.Seq.empty,
          releaseNotes = None,
          snapshots = None,
          levels = List(
            Level(
              SampleReadinessLevels.Incubating,
              LocalDate.of(2018, 11, 22),
              "0.18",
              None,
              Some("Community rocks this module")
            )
          )
        )
      )
    }

    "support history" in {
      val in =
        s"""
           |project-info {
           |  issues-url: "https://github.com/akka/alpakka/labels/"
           |  core: {
           |    title: "The core project"
           |    scala-versions: ["2.12", "2.13"]
           |    jdk-versions: ["OpenJDK 8"]
           |    jpms-name: "alpakka.core"
           |    issues: {
           |      url: $${project-info.issues-url}"p:core"
           |    }
           |    levels: [
           |      {
           |        readiness: Supported
           |        since: "2018-12-12"
           |        since-version: "0.21"
           |      }
           |      {
           |        readiness: Incubating
           |        since: "2018-11-22"
           |        since-version: "0.18"
           |        note: "Community rocks this module"
           |      }
           |    ]
           |  }
           |}
        """.stripMargin
      val c    = ConfigFactory.parseString(in).resolve().getConfig("project-info")
      val name = "core"
      val conf = c.getConfig(name)
      ProjectInfo(name, SampleReadinessLevels.values, conf) should be(
        ProjectInfo(
          "core",
          title = "The core project",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          Some("alpakka.core"),
          issues = Some(Link("https://github.com/akka/alpakka/labels/p:core", None)),
          apiDocs = immutable.Seq.empty,
          forums = immutable.Seq.empty,
          releaseNotes = None,
          snapshots = None,
          levels = List(
            Level(SampleReadinessLevels.Supported, LocalDate.of(2018, 12, 12), "0.21", None, None),
            Level(
              SampleReadinessLevels.Incubating,
              LocalDate.of(2018, 11, 22),
              "0.18",
              None,
              Some("Community rocks this module")
            )
          )
        )
      )
    }

    "support API docs as list" in {
      val in =
        s"""
           |project-info {
           |  issues-url: "https://github.com/akka/alpakka/labels/"
           |  core: {
           |    title: "The core project"
           |    scala-versions: ["2.12", "2.13"]
           |    jdk-versions: ["OpenJDK 8"]
           |    jpms-name: "alpakka.core"
           |    issues: {
           |      url: $${project-info.issues-url}"p:core"
           |    }
           |    api-docs: [
           |      {
           |        text: "Javadoc"
           |        url: "https://developer.lightbend.com/docs/api/alpakka/current/akka/stream/alpakka/index.html"
           |      }
           |      {
           |        text: "Scaladoc"
           |        url: "https://developer.lightbend.com/docs/api/alpakka/current/akka/stream/alpakka/index.html"
           |      }
           |    ]
           |    levels: [
           |      {
           |        readiness: Supported
           |        since: "2018-12-12"
           |        since-version: "0.21"
           |      }
           |    ]
           |  }
           |}
        """.stripMargin
      val c    = ConfigFactory.parseString(in).resolve().getConfig("project-info")
      val name = "core"
      val conf = c.getConfig(name)
      ProjectInfo(name, SampleReadinessLevels.values, conf) should be(
        ProjectInfo(
          "core",
          title = "The core project",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          Some("alpakka.core"),
          issues = Some(Link("https://github.com/akka/alpakka/labels/p:core", None)),
          apiDocs = List(
            Link(
              "https://developer.lightbend.com/docs/api/alpakka/current/akka/stream/alpakka/index.html",
              Some("Javadoc")
            ),
            Link(
              "https://developer.lightbend.com/docs/api/alpakka/current/akka/stream/alpakka/index.html",
              Some("Scaladoc")
            )
          ),
          forums = immutable.Seq.empty,
          releaseNotes = None,
          snapshots = None,
          levels = List(
            Level(SampleReadinessLevels.Supported, LocalDate.of(2018, 12, 12), "0.21", None, None)
          )
        )
      )
    }

    "support forums as list" in {
      val in =
        s"""
           |project-info {
           |  issues-url: "https://github.com/akka/alpakka/labels/"
           |  core: {
           |    title: "The core project"
           |    scala-versions: ["2.12", "2.13"]
           |    jdk-versions: ["OpenJDK 8"]
           |    jpms-name: "alpakka.core"
           |    issues: {
           |      url: $${project-info.issues-url}"p:core"
           |    }
           |    forums: [
           |      {
           |        text: "Lightbend Discuss"
           |        url: "https://discuss.lightbend.com/c/akka/"
           |      }
           |      {
           |        text: "akka/alpakka-kafka Gitter channel"
           |        url: "https://gitter.im/akka/alpakka-kafka"
           |      }
           |    ]
           |    levels: [
           |      {
           |        readiness: Supported
           |        since: "2018-12-12"
           |        since-version: "0.21"
           |      }
           |    ]
           |  }
           |}
        """.stripMargin
      val c    = ConfigFactory.parseString(in).resolve().getConfig("project-info")
      val name = "core"
      val conf = c.getConfig(name)
      ProjectInfo(name, SampleReadinessLevels.values, conf) should be(
        ProjectInfo(
          "core",
          title = "The core project",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          Some("alpakka.core"),
          issues = Some(Link("https://github.com/akka/alpakka/labels/p:core", None)),
          apiDocs = immutable.Seq.empty,
          forums = List(
            Link("https://discuss.lightbend.com/c/akka/", Some("Lightbend Discuss")),
            Link("https://gitter.im/akka/alpakka-kafka", Some("akka/alpakka-kafka Gitter channel"))
          ),
          releaseNotes = None,
          snapshots = None,
          levels = List(
            Level(SampleReadinessLevels.Supported, LocalDate.of(2018, 12, 12), "0.21", None, None)
          )
        )
      )
    }

    "support snapshot link" in {
      val in =
        s"""
           |project-info {
           |  core: {
           |    title: "The core project"
           |    scala-versions: ["2.12", "2.13"]
           |    jdk-versions: ["OpenJDK 8"]
           |    snapshots: {
           |      url: "https://bintray.com/akka/snapshots/alpakka-kafka/1.0-M1%2B44-9f512541"
           |      text: "Snapshots are published after every commit on master"
           |      new-tab: false
           |    }
           |    levels: [
           |      {
           |        readiness: Supported
           |        since: "2018-12-12"
           |        since-version: "0.21"
           |      }
           |    ]
           |  }
           |}
        """.stripMargin
      val c    = ConfigFactory.parseString(in).resolve().getConfig("project-info")
      val name = "core"
      val conf = c.getConfig(name)
      ProjectInfo(name, SampleReadinessLevels.values, conf) should be(
        ProjectInfo(
          "core",
          title = "The core project",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions = List("OpenJDK 8"),
          jpmsName = None,
          issues = None,
          apiDocs = immutable.Seq.empty,
          forums = immutable.Seq.empty,
          releaseNotes = None,
          snapshots = Some(
            Link(
              "https://bintray.com/akka/snapshots/alpakka-kafka/1.0-M1%2B44-9f512541",
              Some("Snapshots are published after every commit on master"),
              newTab = false
            )
          ),
          levels = List(
            Level(SampleReadinessLevels.Supported, LocalDate.of(2018, 12, 12), "0.21", None, None)
          )
        )
      )
    }

  }

}
