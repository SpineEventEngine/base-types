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

@file:Suppress("RemoveRedundantQualifierName")

import io.spine.dependency.boms.BomsPlugin
import io.spine.dependency.build.Dokka
import io.spine.dependency.build.JSpecify
import io.spine.dependency.lib.Grpc
import io.spine.dependency.lib.Jackson
import io.spine.dependency.lib.Kotlin
import io.spine.dependency.lib.KotlinPoet
import io.spine.dependency.lib.Protobuf
import io.spine.dependency.local.Base
import io.spine.dependency.local.Compiler
import io.spine.dependency.local.CoreJava
import io.spine.dependency.local.Logging
import io.spine.dependency.local.ProtoData
import io.spine.dependency.local.ToolBase
import io.spine.dependency.local.Validation
import io.spine.gradle.checkstyle.CheckStyleConfig
import io.spine.gradle.javadoc.JavadocConfig
import io.spine.gradle.publish.IncrementGuard
import io.spine.gradle.publish.PublishingRepos
import io.spine.gradle.publish.PublishingRepos.gitHub
import io.spine.gradle.publish.spinePublishing
import io.spine.gradle.repo.standardToSpineSdk
import io.spine.gradle.report.license.LicenseReporter
import io.spine.gradle.report.pom.PomGenerator

buildscript {
    apply(from = "$projectDir/version.gradle.kts")
    standardSpineSdkRepositories()

    dependencies {
        // Put the plugin before in the classpath to avoid complaints about the version.
        classpath(io.spine.dependency.build.Ksp.gradlePlugin)
        classpath(io.spine.dependency.local.CoreJvmCompiler.pluginLib)
    }

    configurations {
        all {
            resolutionStrategy {
                force(
                    io.spine.dependency.lib.Kotlin.bom,
                    io.spine.dependency.build.Dokka.BasePlugin.lib,
                    io.spine.dependency.local.Base.lib,
                )
            }
        }
    }
}

plugins {
    `jvm-module`
    protobuf
    ksp
    `module-testing`
}
apply<BomsPlugin>()

// Cannot use `id()` syntax for CoreJvm Compiler because it's not yet published to the Plugin Portal
// and is added to the build classpath via the `buildScript` block above.
apply(plugin = "io.spine.core-jvm")

apply<IncrementGuard>()

apply(from = "$projectDir/version.gradle.kts")
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

repositories {
    standardToSpineSdk()
}

configurations {
    forceVersions()
    excludeProtobufLite()

    all {
        resolutionStrategy {
            Grpc.forceArtifacts(project, this@all, this@resolutionStrategy)
            Jackson.forceArtifacts(project, this@all, this@resolutionStrategy)
            Jackson.DataType.forceArtifacts(project, this@all, this@resolutionStrategy)
            force(
                Base.annotations,
                Base.lib,
                Compiler.api,
                CoreJava.server,
                Dokka.BasePlugin.lib,
                Jackson.annotations,
                JSpecify.annotations,
                Kotlin.bom,
                KotlinPoet.lib,
                Logging.lib,
                ProtoData.api,
                Protobuf.compiler,
                ToolBase.gradlePluginApi,
                ToolBase.jvmTools,
                ToolBase.lib,
                ToolBase.pluginBase,
                ToolBase.protobufSetupPlugins,
                ToolBase.psiJava,
                Validation.runtime,
            )
        }
    }
}

dependencies {
    implementation(Base.lib)
    implementation(Validation.runtime)
}

spinePublishing {
    destinations = setOf(
        gitHub("base-types"),
        PublishingRepos.cloudArtifactRegistry
    )

    dokkaJar {
        kotlin = true
        java = true
    }
}

project.configureTaskDependencies()

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.generateReportIn(project)
LicenseReporter.mergeAllReports(project)
