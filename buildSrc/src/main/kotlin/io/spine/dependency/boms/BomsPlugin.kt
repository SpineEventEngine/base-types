/*
 * Copyright 2025, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.dependency.boms

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * The plugin which forces versions of platforms declared in the [Boms] object.
 *
 * Versions are enforced via the
 * [org.gradle.api.artifacts.dsl.DependencyHandler.enforcedPlatform] call
 * for configurations of the project to which the plugin is applied.
 *
 * The configurations are selected by the "kind" of BOM.
 *
 * [Boms.core] are applied to:
 *  1. Production configurations, such as `api` or `implementation`.
 *  2. All `ksp` configurations.
 *
 *  [Boms.testing] are applied to all testing configurations.
 */
class BomsPlugin : Plugin<Project>  {

    private val productionConfigs = listOf(
        "api",
        "implementation",
        "compileOnly",
        "runtimeOnly"
    )

    override fun apply(project: Project) = with(project) {

        fun Configuration.applyBoms(boms: List<String>) {
            withDependencies {
                boms.forEach { bom ->
                    val platform = project.dependencies.enforcedPlatform(bom)
                    addLater( provider { platform })
                    project.logger.info(
                        "Applied BOM: `$bom` to the configuration: `${this@applyBoms.name}`."
                    )
                }
            }
        }

        configurations.run {
            matching { isCompilationConfig(it.name) }.all {
                applyBoms(Boms.core)
            }
            matching { isKspConfig(it.name) }.all {
                applyBoms(Boms.core)
            }
            matching { it.name in productionConfigs }.all {
                applyBoms(Boms.core)
            }
            matching { isTestConfig(it.name) }.all {
                applyBoms(Boms.core + Boms.testing)
            }
        }
    }

    private fun isCompilationConfig(name: String) =
        name.contains("compile", ignoreCase = true)

    private fun isKspConfig(name: String) =
        name.startsWith("ksp", ignoreCase = true)

    private fun isTestConfig(name: String) =
        name.startsWith("test", ignoreCase = true)
}
