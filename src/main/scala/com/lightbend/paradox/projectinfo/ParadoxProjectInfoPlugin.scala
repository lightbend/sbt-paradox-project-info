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

import com.lightbend.paradox.markdown.Writer
import com.lightbend.paradox.sbt.ParadoxPlugin
import com.lightbend.paradox.sbt.ParadoxPlugin.autoImport.paradoxDirectives
import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import sbt.Keys._
import sbt._

object ParadoxProjectInfoPlugin extends AutoPlugin {
  import ParadoxProjectInfoPluginKeys._

  object autoImport extends ParadoxProjectInfoPluginKeys

  override def requires: Plugins = ParadoxPlugin

  override def trigger: PluginTrigger = AllRequirements

  override def projectSettings: Seq[Setting[_]] = projectInfoSettings(Compile)

  override def globalSettings: Seq[Setting[_]] = Seq(
    projectInfoVersion := version.value
  )

  def projectInfoSettings(config: Configuration): Seq[Setting[_]] =
    Seq(
      paradoxDirectives ++= Def.taskDyn {
        Def.task {
          val s = state.value
          Seq { _: Writer.Context ⇒
            val f           = new File((LocalRootProject / baseDirectory).value, "project/project-info.conf")
            val extracted   = Project.extract(s)
            val rootVersion = extracted.get(projectInfoVersion)
            val config = if (f.exists()) {
              ConfigFactory
                .parseFile(f)
                // inject into config before resolving
                .withValue("project-info.version", ConfigValueFactory.fromAnyRef(rootVersion))
                .resolve()
                .getConfig("project-info")
            } else {
              sLog.value.warn(s"Could not retrieve project-info from ${f.absolutePath}")
              ConfigFactory.empty()
            }
            val sbtDetails: String => SbtValues = projectId => {
              val project = LocalProject(projectId)
              val projectName =
                try extracted.get(project / name)
                catch {
                  case e: Exception =>
                    throw new RuntimeException(
                      s"couldn't read sbt setting `$projectId / name`, does the projectId exist?"
                    )
                }
              SbtValues(
                projectName,
                extracted.get(project / version),
                extracted.get(project / organization),
                extracted.get(project / homepage),
                extracted.get(project / scmInfo),
                extracted.get(project / licenses).toList,
                extracted.get(project / crossScalaVersions).toList
              )
            }
            new ProjectInfoDirective(config, sbtDetails)
          }
        }
      }.value
    ) ++ inConfig(config)(
      Seq(
        // scoped settings here
      )
    )
}
