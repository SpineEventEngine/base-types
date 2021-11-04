/*
 * Copyright 2021, TeamDev. All rights reserved.
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

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.remove
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.JUnit
import io.spine.internal.gradle.IncrementGuard
import io.spine.internal.gradle.JavadocConfig
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.report.coverage.JacocoConfig
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.spinePublishing
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("RemoveRedundantQualifierName") // cannot use imports under `buildScript`
buildscript {
    apply(from = "$projectDir/version.gradle.kts")
    val spineVersion: String by extra

    io.spine.internal.gradle.doApplyStandard(repositories)

    dependencies {
        classpath("io.spine.tools:spine-mc-java:$spineVersion")
    }
}

plugins {
    `java-library`
    kotlin("jvm") version io.spine.internal.dependency.Kotlin.version
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id) version version
    }
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id)
    }
    pmd
    jacoco
    `force-jacoco`
    `project-report`
    `pmd-settings`
}

repositories.applyStandard()
configurations.forceVersions()
configurations.excludeProtobufLite()

apply(plugin = "io.spine.mc-java")

apply(from = "$projectDir/version.gradle.kts")
val spineVersion: String by extra
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

apply<IncrementGuard>()

spinePublishing {
    targetRepositories.addAll(setOf(
        PublishingRepos.gitHub("base-types"),
        PublishingRepos.cloudArtifactRegistry
    ))
    publish(project)
}

// The dependencies should be similar to those defined in the `../build.gradle.kts`.
dependencies {
    errorprone(ErrorProne.core)
    errorproneJavac(ErrorProne.javacPlugin)

    api("io.spine:spine-base:$spineVersion")

    testImplementation(JUnit.runner)
    testImplementation(JUnit.pioneer)
    testImplementation("io.spine.tools:spine-testlib:$spineVersion")
}

val javaVersion = JavaVersion.VERSION_1_8

the<JavaPluginExtension>().apply {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile> {
    configureJavac()
    configureErrorProne()
}

CheckStyleConfig.applyTo(project)
LicenseReporter.generateReportIn(project)
JavadocConfig.applyTo(project)

kotlin {
    explicitApi()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = javaVersion.toString()
        freeCompilerArgs = listOf("-Xskip-prerelease-check")
    }
}

protobuf {
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                remove("grpc")
            }
        }
    }
}

updateGitHubPages {
    allowInternalJavadoc.set(true)
    rootFolder.set(rootDir)
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

JacocoConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.mergeAllReports(project)
