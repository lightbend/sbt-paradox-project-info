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

import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.immutable
import scala.collection.JavaConverters._

case class SbtValues(artifact: String,
                     version: String,
                     organization: String,
                     homepage: Option[sbt.URL],
                     scmInfo: Option[sbt.librarymanagement.ScmInfo],
                     licenses: immutable.Seq[(String, sbt.URL)],
                     crossScalaVersions: immutable.Seq[String])

trait ReadinessLevel { def name: String }
object ReadinessLevel {
  private def glossary(anchor: String, label: String): String =
    s"""<a href="https://developer.lightbend.com/docs/reactive-platform/2.0/support-terminology/index.html#$anchor" target="_blank" rel="noopener noreferrer">$label</a>""".stripMargin

  case object Supported extends ReadinessLevel {
    val name =
      s"""${glossary("supported", "Supported")}, <a href="https://www.lightbend.com/subscription" target="_blank">Lightbend Subscription</a> provides support"""
  }
  case object Certified extends ReadinessLevel {
    val name =
      s"""${glossary("certified", "Certified")} by <a href="https://www.lightbend.com/" target="_blank">Lightbend</a>"""
  }
  case object Incubating extends ReadinessLevel {
    val name = glossary("incubating", "Incubating")
  }
  case object CommunityDriven extends ReadinessLevel {
    val name = glossary("community-driven", "Community-driven")
  }
  case object EndOfLife extends ReadinessLevel {
    val name =
      s"${glossary("end-of-life-eol-", "End-of-Life")}, it is not recommended to use this project any more."
  }

  def fromString(s: String): ReadinessLevel = s match {
    case "Supported" => Supported
    case "Certified" => Certified
    case "Incubating" => Incubating
    case "CommunityDriven" => CommunityDriven
    case "EndOfLife" => EndOfLife
    case other => throw new IllegalArgumentException(s"unknown readiness level: $other")
  }
}

case class Link(url: String, text: Option[String], newTab: Boolean = true)

object Link {
  def apply(c: Config): Link = {
    import Util.ExtendedConfig
    val url    = c.getString("url")
    val text   = c.getOption("text", _.getString(_))
    val newTab = c.getOptionalBoolean("new-tab", true)
    Link(url, text, newTab)
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
                       apiDocs: immutable.Seq[Link],
                       forums: immutable.Seq[Link],
                       releaseNotes: Option[Link],
                       snapshots: Option[Link],
                       levels: immutable.Seq[Level])

object ProjectInfo {
  import Util.ExtendedConfig

  def apply(name: String, c: Config): ProjectInfo = {
    val title = c.getOption("title", _.getString(_)).getOrElse(name)
    val scalaVersions =
      if (c.hasPath("scala-versions")) c.getStringList("scala-versions").asScala.toList
      else immutable.Seq.empty
    val jdkVersions  = c.getStringList("jdk-versions").asScala.toList
    val jpmsName     = c.getOption("jpms-name", _.getString(_))
    val issues       = c.getOption("issues", (c, s) => Link(c.getConfig(s)))
    val apiDocs      = c.getParsedList("api-docs", c => Link(c))
    val forums       = c.getParsedList("forums", c => Link(c))
    val releaseNotes = c.getOption("release-notes", (c, s) => Link(c.getConfig(s)))
    val snapshots    = c.getOption("snapshots", (c, s) => Link(c.getConfig(s)))
    val levels =
      for { item <- c.getObjectList("levels").asScala.toList } yield {
        Level(item.toConfig)
      }
    new ProjectInfo(name,
                    title,
                    scalaVersions,
                    jdkVersions,
                    jpmsName,
                    issues,
                    apiDocs,
                    forums,
                    releaseNotes,
                    snapshots,
                    levels)
  }
}

case class SbtAndProjectInfo(sbtValues: SbtValues, projectInfo: ProjectInfo)

object Util {
  private val dateFormat = DateTimeFormatter.ISO_LOCAL_DATE

  implicit class ExtendedConfig(c: Config) {
    def getReadinessLevel(path: String): ReadinessLevel = ReadinessLevel.fromString(c.getString(path))
    def getLocalDate(path: String): LocalDate           = LocalDate.parse(c.getString(path), dateFormat)
    def getOption[T](path: String, read: (Config, String) => T): Option[T] =
      if (c.hasPath(path)) Some(read(c, path)) else None
    def getOptionalBoolean(path: String, defaultValue: Boolean): Boolean =
      if (c.hasPath(path)) c.getBoolean(path) else defaultValue
    def getParsedList[T](path: String, read: Config => T): List[T] =
      if (c.hasPath(path)) {
        for (cObj <- c.getObjectList(path).asScala.toList) yield {
          read(cObj.toConfig)
        }
      } else List.empty
  }
}

object ProjectInfoReader {

  def readConfig(sbtBaseDir: File): Either[String, Config] = {
    val f = new File(sbtBaseDir, "project/project-info.conf")
    if (f.exists()) {
      Right(ConfigFactory.parseFile(f).resolve().getConfig("project-info"))
    } else Left(s"Could not retrieve project-info from ${f.getAbsolutePath}")
  }
}
