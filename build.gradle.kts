/*
 * Copyright 2022, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.remove
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.JUnit
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.JavadocConfig
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.publish.Publish.Companion.publishProtoArtifact
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.PublishingRepos.gitHub
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.spinePublishing
import io.spine.internal.gradle.test.configureLogging
import io.spine.internal.gradle.test.registerTestTasks
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    apply(from = "$projectDir/version.gradle.kts")
    io.spine.internal.gradle.doApplyStandard(repositories)
    repositories {
        io.spine.internal.gradle.publish.PublishingRepos.gitHub("mc-java")
    }

    val mcJavaVersion: String by extra
    val spineBaseVersion: String by extra
    val spineTimeVersion: String by extra
    dependencies {
        classpath("io.spine.tools:spine-mc-java:$mcJavaVersion")
    }
    configurations {
        all {
            resolutionStrategy {
                force(
                    "io.spine:spine-base:$spineBaseVersion",
                    "io.spine:spine-time:$spineTimeVersion",
                )
            }
        }
    }
}

plugins {
    `java-library`
    kotlin("jvm")
    idea
    id(io.spine.internal.dependency.Protobuf.GradlePlugin.id)
    id(io.spine.internal.dependency.ErrorProne.GradlePlugin.id)
    pmd
    jacoco
    `force-jacoco`
    `project-report`
    `pmd-settings`
}

val spineBaseVersion: String by extra
val spineTimeVersion: String by extra

repositories.applyStandard()
configurations.forceVersions()
configurations.excludeProtobufLite()
configurations {
    all {
        resolutionStrategy {
            force(
                "io.spine:spine-base:$spineBaseVersion",
                "io.spine:spine-time:$spineTimeVersion",
            )
        }
    }
}

apply(plugin = "io.spine.mc-java")

apply(from = "$projectDir/version.gradle.kts")
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

apply<IncrementGuard>()
apply<VersionWriter>()

spinePublishing {
    targetRepositories.addAll(
        setOf(
            gitHub("base-types"),
            PublishingRepos.cloudRepo,
            PublishingRepos.cloudArtifactRegistry
        )
    )
    publish(project)
}

val javaVersion = JavaVersion.VERSION_11

the<JavaPluginExtension>().apply {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile> {
    configureJavac()
    configureErrorProne()
}

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javaVersion.toString()
        freeCompilerArgs = listOf("-Xskip-prerelease-check")
    }
}

// The dependencies should be similar to those defined in the `../build.gradle.kts`.
dependencies {
    errorprone(ErrorProne.core)

    api("io.spine:spine-base:$spineBaseVersion")

    testImplementation(JUnit.runner)
    testImplementation("io.spine.tools:spine-testlib:$spineBaseVersion")
}

val generatedDir = "$projectDir/generated"

protobuf {
    generatedFilesBaseDir = generatedDir
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                remove("grpc")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    configureJavac()
    configureErrorProne()
}

val javadocToolsVersion: String by extra
updateGitHubPages(javadocToolsVersion) {
    allowInternalJavadoc.set(true)
    rootFolder.set(rootDir)
}

tasks {
    registerTestTasks()
    test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        configureLogging()
    }
}
CheckStyleConfig.applyTo(project)
publishProtoArtifact(project)
JavadocConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.generateReportIn(project)
LicenseReporter.mergeAllReports(project)
