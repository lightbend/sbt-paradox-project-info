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

import java.time.format.DateTimeFormatter

import com.lightbend.paradox.markdown.LeafBlockDirective
import com.typesafe.config.Config
import org.pegdown.Printer
import org.pegdown.ast.{DirectiveNode, Visitor}

class ProjectInfoDirective(config: Config, moduleToSbtValues: String => SbtValues)
    extends LeafBlockDirective("project-info") {
  def render(node: DirectiveNode, visitor: Visitor, printer: Printer): Unit = {
    val projectId = node.attributes.value("projectId")
    if (config.hasPath(projectId)) {
      val module = config.getConfig(projectId)
      val data   = SbtAndProjectInfo(moduleToSbtValues(projectId), ProjectInfo(projectId, module))
      ProjectInfoDirective.renderInfo(printer, data)
    } else throw new RuntimeException(s"project-info.conf does not contain `$projectId`")
  }
}

object ProjectInfoDirective {
  def printLink(p: Printer, url: String, text: String, newTab: Boolean): Unit = {
    p.print("<a href=\"")
      .print(url)
      .print("\"")
    if (newTab) {
      p.print(" target=\"_blank\" rel=\"noopener noreferrer\"")
    }
    p.print(">")
      .print(text)
      .print("</a>")
  }

  val formatter = DateTimeFormatter.ISO_LOCAL_DATE
  def renderInfo(p: Printer, data: SbtAndProjectInfo): Unit = {
    import data.projectInfo._
    import data.sbtValues
    p.printchkln()
    p.print("""<table class="project-info">""").println()
    p.indent(2)
    p.print("""<tr><th colspan="2">Project Info: """).print(title).print("</th></tr>").println()
    p.print("<tr><th>Artifact</th><td><div>")
      .print(sbtValues.organization)
      .print("</div>")
      .println()
      .print("<div>")
      .print(sbtValues.artifact)
      .print("</div>")
      .println()
      .print("<div>")
      .print(sbtValues.version)
      .print("</div>")
    snapshots.foreach { case Link(url, text, newTab) =>
      p.println().print("<div>")
      printLink(p, url, text.getOrElse("Snapshots"), newTab)
      p.print("</div>").println()
    }
    p.print("</td></tr>")
      .println()
    if (jdkVersions.nonEmpty) {
      p.print("<tr><th>JDK versions</th><td>")
        .print(jdkVersions.mkString("<div>", "</div><div>", "</div>"))
        .print("</td></tr>")
        .println()
    }
    // scala-versions from project-info.conf overwrites sbt's crossScalaVersions
    if (scalaVersions.nonEmpty) {
      p.print("<tr><th>Scala versions</th><td>")
        .print(scalaVersions.mkString(", "))
        .print("</td></tr>")
        .println()
    } else if (sbtValues.crossScalaVersions.nonEmpty) {
      p.print("<tr><th>Scala versions</th><td>")
        .print(sbtValues.crossScalaVersions.mkString(", "))
        .print("</td></tr>")
        .println()
    }
    jpmsName.foreach(n => p.print("<tr><th>JPMS module name</th><td>").print(n).print("</td></tr>").println())
    if (sbtValues.licenses.nonEmpty) {
      p.print("<tr><th>License</th><td>")
      for {
        lic <- sbtValues.licenses
      } {
        p.print("<div>")
        printLink(p, lic._2.toString, lic._1, newTab = true)
        p.print("</div>").println()
      }
      p.print("</td></tr>").println()
    }
    levels.headOption.foreach { currentLevel =>
      p.print("<tr><th>Readiness level</th><td>")
      p.print("""<div class="readiness-level">""").print(currentLevel.level.name).print("</div>").println()
      p.print("<div>Since ")
        .print(currentLevel.sinceVersion)
        .print(", ")
        .print(currentLevel.since.format(formatter))
        .print("</div>")
        .println()
      currentLevel.ends.foreach { endDate =>
        p.print("<div>Ends: ").print(endDate.format(formatter)).print("</div>").println()
      }
      currentLevel.note.foreach { text =>
        p.print("<div>Note: ").printEncoded(text).print("</div>").println()
      }
      p.print("</td></tr>")
    }

    sbtValues.homepage.foreach { page =>
      p.println().print("<tr><th>Home page</th><td>")
      printLink(p, page.toExternalForm, page.toExternalForm, false)
      p.print("</td></tr>")
    }
    if (apiDocs.nonEmpty) {
      p.println().print("<tr><th>API documentation</th><td>")
      apiDocs.foreach { case Link(url, text, newTab) =>
        p.println().print("<div>")
        printLink(p, url, text.getOrElse("API"), newTab)
        p.print("</div>")
      }
      p.println().print("</td></tr>")
    }
    if (forums.nonEmpty) {
      p.println().print("<tr><th>Forums</th><td>")
      forums.foreach { case Link(url, text, newTab) =>
        p.println().print("<div>")
        printLink(p, url, text.getOrElse("Forum"), newTab)
        p.print("</div>")
      }
      p.println().print("</td></tr>")
    }
    releaseNotes.foreach { case Link(url, text, newTab) =>
      p.println().print("<tr><th>Release notes</th><td>")
      printLink(p, url, text.getOrElse("Release notes"), newTab)
      p.print("</td></tr>")
    }
    issues.foreach { case Link(url, text, newTab) =>
      p.println().print("<tr><th>Issues</th><td>")
      printLink(p, url, text.getOrElse("Issue tracker"), newTab)
      p.print("</td></tr>")
    }
    sbtValues.scmInfo.foreach { scmInfo =>
      p.println().print("<tr><th>Sources</th><td>")
      printLink(p, scmInfo.browseUrl.toExternalForm, scmInfo.browseUrl.toExternalForm, newTab = true)
      p.print("</td></tr>")
    }
    p.indent(-2).println()
    p.print("</table>").println()
  }

}
