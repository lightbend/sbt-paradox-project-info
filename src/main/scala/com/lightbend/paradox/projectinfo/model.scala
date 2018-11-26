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
import java.time.format.DateTimeFormatter

import com.typesafe.config.Config

import scala.collection.immutable
import scala.collection.JavaConverters._

case class SbtValues(artifact: String, version: String)

trait ReadinessLevel { def name: String }
object ReadinessLevel {
  case object Supported extends ReadinessLevel {
    val name = "<a href=\"https://www.lightbend.com/subscription\">Lightbend</a> provides Developer Support"
  }
  case object Certified extends ReadinessLevel {
    val name = "Certified by <a href=\"https://www.lightbend.com/\">Lightbend</a>"
  }
  case object Incubating extends ReadinessLevel {
    val name = "Incubating"
  }
  case object EndOfLife extends ReadinessLevel {
    val name = "End-of-Life, it is not recommended to use this project any more."
  }

  def fromString(s: String): ReadinessLevel = s match {
    case "Supported" => Supported
    case "Certified" => Certified
    case "Incubating" => Incubating
    case "EndOfLife" => EndOfLife
    case other => throw new IllegalArgumentException(s"unknown readiness level: $other")
  }
}

case class Link(url: String, text: Option[String])

object Link {
  def apply(c: Config): Link = {
    import Util.ExtendedConfig
    val url  = c.getString("url")
    val text = c.getOption("text", _.getString(_))
    Link(url, text)
  }
}

case class Level(level: ReadinessLevel,
                 since: LocalDate,
                 sinceVersion: String,
                 ends: Option[LocalDate],
                 note: Option[String])

object Level {

  def apply(c: Config): Level = {
    import Util.ExtendedConfig
    val ml           = c.getReadinessLevel("readiness")
    val since        = c.getLocalDate("since")
    val sinceVersion = c.getString("since-version")
    val ends         = c.getOption("ends", _.getLocalDate(_))
    val note         = c.getOption("note", _.getString(_))
    Level(ml, since, sinceVersion, ends, note)
  }
}

case class ProjectInfo(name: String,
                       title: String,
                       scalaVersions: immutable.Seq[String],
                       jdkVersions: immutable.Seq[String],
                       jpmsName: Option[String],
                       issues: Option[Link],
                       releaseNotes: Option[Link],
                       levels: immutable.Seq[Level])

object ProjectInfo {
  import Util.ExtendedConfig

  def apply(name: String, c: Config): ProjectInfo = {
    val title         = c.getOption("title", _.getString(_)).getOrElse(name)
    val scalaVersions = c.getStringList("scala-versions").asScala.toList
    val jdkVersions   = c.getStringList("jdk-versions").asScala.toList
    val jpmsName      = c.getOption("jpms-name", _.getString(_))
    val issues        = c.getOption("issues", (c, s) => Link(c.getConfig(s)))
    val releaseNotes  = c.getOption("release-notes", (c, s) => Link(c.getConfig(s)))
    val levels =
      for { item <- c.getObjectList("levels").asScala.toList } yield {
        Level(item.toConfig)
      }
    new ProjectInfo(name, title, scalaVersions, jdkVersions, jpmsName, issues, releaseNotes, levels)
  }
}

object Util {
  private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit class ExtendedConfig(c: Config) {
    def getReadinessLevel(path: String): ReadinessLevel = ReadinessLevel.fromString(c.getString(path))
    def getLocalDate(path: String): LocalDate           = LocalDate.parse(c.getString(path), dateFormat)
    def getOption[T](path: String, read: (Config, String) => T): Option[T] =
      if (c.hasPath(path)) Some(read(c, path)) else None
  }
}
