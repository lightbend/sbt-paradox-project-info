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
import org.scalatest.WordSpec
import org.scalatest.Matchers

class ProjectInfoSpec extends WordSpec with Matchers {

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
      Level(c) should be(Level(ReadinessLevel.Incubating, LocalDate.of(2018, 11, 22), "0.12", None, None))
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
          |  levels: [
          |    {
          |      readiness: Incubating
          |      since: "2018-11-22"
          |      since-version: "12.2"
          |      note: "Community rocks this module"
          |    }
          |  ]
          |}
        """.stripMargin
      val c = ConfigFactory.parseString(in).getConfig("core")
      ProjectInfo("core", c) should be(
        ProjectInfo(
          "core",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions   = List("OpenJDK 8"),
          Some("alpakka.core"),
          issuesUrl = None,
          levels = List(
            Level(ReadinessLevel.Incubating,
                  LocalDate.of(2018, 11, 22),
                  "12.2",
                  None,
                  Some("Community rocks this module"))),
        ))
    }
  }

  "Sub-project" should {
    "read from config" in {
      val in =
        """
          |project-info {
          |  core: {
          |    version: "1.0.1"
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
      ProjectInfo(name, conf) should be(
        ProjectInfo(
          "core",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions   = List("OpenJDK 8"),
          Some("alpakka.core"),
          issuesUrl = None,
          levels = List(
            Level(ReadinessLevel.Incubating,
                  LocalDate.of(2018, 11, 22),
                  "0.18",
                  None,
                  Some("Community rocks this module"))),
        ))
    }

    "support history" in {
      val in =
        s"""
           |project-info {
           |  issues-url: "https://github.com/akka/alpakka/labels/"
           |  core: {
           |    version: "1.0.1"
           |    scala-versions: ["2.12", "2.13"]
           |    jdk-versions: ["OpenJDK 8"]
           |    jpms-name: "alpakka.core"
           |    issues-url: $${project-info.issues-url}"p:core"
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
      ProjectInfo(name, conf) should be(
        ProjectInfo(
          "core",
          scalaVersions = List("2.12", "2.13"),
          jdkVersions   = List("OpenJDK 8"),
          Some("alpakka.core"),
          issuesUrl = Some("https://github.com/akka/alpakka/labels/p:core"),
          levels = List(
            Level(ReadinessLevel.Supported, LocalDate.of(2018, 12, 12), "0.21", None, None),
            Level(ReadinessLevel.Incubating,
                  LocalDate.of(2018, 11, 22),
                  "0.18",
                  None,
                  Some("Community rocks this module"))
          )
        ))
    }
  }

}
