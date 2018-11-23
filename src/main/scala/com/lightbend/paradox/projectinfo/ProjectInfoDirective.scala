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
    val moduleName = node.attributes.value("sub-project")
    val module     = config.getConfig(moduleName)
    val data       = ProjectInfo(moduleName, module)
    val sbtValues  = moduleToSbtValues(moduleName)
    ProjectInfoDirective.renderInfo(printer, data, sbtValues)
  }
}

object ProjectInfoDirective {
  val formatter = DateTimeFormatter.ISO_LOCAL_DATE
  def renderInfo(p: Printer, data: ProjectInfo, sbtValues: SbtValues): Unit = {
    import data._
    p.printchkln()
    p.print("""<table class="project-info">""").println()
    p.indent(2)
    p.print("<tr><th>Artifact name</th><td>").print(sbtValues.artifact).print("</td></tr>").println()
    p.print("<tr><th>Version</th><td>").print(sbtValues.version).print("</th></tr>").println()
    jpmsName.foreach(n => p.print("<tr><th>JPMS module name</th><td>").print(n).print("</td></tr>").println())
    p.print("<tr><th>Readiness level</th><td>")
    val currentLevel = levels.head
    p.print("<div class='readiness-level'>").print(currentLevel.level.name).print("</div>").println()
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
    p.indent(-2).println()
    p.print("</table>").println()
  }

}
