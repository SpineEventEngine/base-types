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
import io.spine.internal.dependency.Dokka
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.JUnit
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.PublishingRepos.gitHub
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.publish.spinePublishing
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
    val baseVersion: String by extra
    val timeVersion: String by extra
    dependencies {
        classpath("io.spine.tools:spine-mc-java-plugins:${mcJavaVersion}:all")
    }

    val dokka = io.spine.internal.dependency.Dokka
    configurations {
        all {
            resolutionStrategy {
                force(
                    "org.jetbrains.dokka:dokka-base:${dokka.version}",
                    "io.spine:spine-base:$baseVersion",
                    "io.spine:spine-time:$timeVersion",
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
    `dokka-for-java`
}

apply(from = "$projectDir/version.gradle.kts")
val baseVersion: String by extra
val timeVersion: String by extra
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

repositories {
    applyStandard()
}

configurations {
    forceVersions()
    excludeProtobufLite()

    all {
        resolutionStrategy {
            force(
                "org.jetbrains.dokka:dokka-base:${Dokka.version}",
                "io.spine:spine-base:$baseVersion",
                "io.spine:spine-time:$timeVersion",
            )
        }
    }
}

apply {
    plugin("jacoco")
    plugin("io.spine.mc-java")
}
apply<IncrementGuard>()
apply<VersionWriter>()

dependencies {
    errorprone(ErrorProne.core)

    implementation("io.spine:spine-base:$baseVersion")
    implementation("io.spine:spine-validate:$baseVersion")

    testImplementation(JUnit.runner)
    testImplementation("io.spine.tools:spine-testlib:$baseVersion")
}

spinePublishing {
    destinations = setOf(
        gitHub("base-types"),
        PublishingRepos.cloudRepo,
        PublishingRepos.cloudArtifactRegistry
    )

    dokkaJar {
        enabled = true
    }
}

val javaVersion = JavaVersion.VERSION_11

java {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    tasks {
        withType<JavaCompile>().configureEach {
            configureJavac()
            configureErrorProne()
        }
    }
}

kotlin {
    explicitApi()

    tasks {
        withType<KotlinCompile>().configureEach {
            kotlinOptions {
                jvmTarget = javaVersion.toString()
                freeCompilerArgs = listOf("-Xskip-prerelease-check")
            }
        }
    }
}

protobuf {
    generatedFilesBaseDir = "$projectDir/generated"
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                remove("grpc")
            }
        }
    }
}

val javadocToolsVersion: String by extra
updateGitHubPages(javadocToolsVersion) {
    allowInternalJavadoc.set(true)
    rootFolder.set(rootDir)
}


tasks {
    registerTestTasks()
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
        }
    }
    test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
        configureLogging()
        finalizedBy(jacocoTestReport)
    }
}

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.generateReportIn(project)
LicenseReporter.mergeAllReports(project)
