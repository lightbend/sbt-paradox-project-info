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
import com.typesafe.config.ConfigFactory
import sbt._
import sbt.Keys._

object ParadoxProjectInfoPlugin extends AutoPlugin {
  object autoImport extends ParadoxProjectInfoPluginKeys
  import autoImport._

  override def requires: Plugins = ParadoxPlugin

  override def trigger: PluginTrigger = AllRequirements

  override def projectSettings: Seq[Setting[_]] = projectInfoSettings(Compile)

  def projectInfoGlobalSettings: Seq[Setting[_]] = Seq(
    paradoxDirectives ++= Def.taskDyn {
      Def.task {
        val s = state.value
        Seq(
          {
            _: Writer.Context ⇒
              val f = new File((LocalRootProject / baseDirectory).value, "project/project-info.conf")
              if (f.exists()) {
                val config    = ConfigFactory.parseFile(f).resolve().getConfig("project-info")
                val extracted = Project.extract(s)
                val sbtDetails: String => SbtValues = projectName => {
                  val project = LocalProject(projectName)
                  SbtValues(
                    extracted.get(project / name),
                    extracted.get(project / version),
                    extracted.get(project / organization),
                    extracted.get(project / homepage),
                    extracted.get(project / scmInfo),
                    extracted.get(project / licenses).toList,
                    extracted.get(project / crossScalaVersions).toList,
                  )
                }
                new ProjectInfoDirective(config, sbtDetails)
              } else {
                throw new Error(s"Could not retrieve project-info from ${f.absolutePath}")
              }
          }
        )
      }
    }.value
  )

  def projectInfoSettings(config: Configuration): Seq[Setting[_]] =
    projectInfoGlobalSettings ++ inConfig(config)(
      Seq(
        // scoped settings here
      ))
}
