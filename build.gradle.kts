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

import io.spine.internal.dependency.Dokka
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.Protobuf
import io.spine.internal.dependency.Spine
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.github.pages.updateGitHubPages
import io.spine.internal.gradle.javac.configureErrorProne
import io.spine.internal.gradle.javac.configureJavac
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.kotlin.setFreeCompilerArgs
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.PublishingRepos.gitHub
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.test.configureLogging
import io.spine.internal.gradle.test.registerTestTasks
import io.spine.tools.mc.gradle.modelCompiler
import io.spine.tools.mc.java.gradle.McJavaOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    apply(from = "$projectDir/version.gradle.kts")
    io.spine.internal.gradle.doApplyStandard(repositories)
    repositories {
        io.spine.internal.gradle.publish.PublishingRepos.gitHub("mc-java")
    }

    dependencies {
        classpath(io.spine.internal.dependency.Spine.McJava.pluginLib)
    }

    val spine = io.spine.internal.dependency.Spine(project)
    configurations {
        all {
            resolutionStrategy {
                force(
                    io.spine.internal.dependency.Dokka.BasePlugin.lib,
                    spine.base,
                )
            }
        }
    }
}

plugins {
    `java-library`
    kotlin("jvm")
    idea
    protobuf
    errorprone
    pmd
    jacoco
    `project-report`
    `pmd-settings`
    `dokka-for-java`
    protodata
    `detekt-code-analysis`
}

apply(from = "$projectDir/version.gradle.kts")
val baseVersion: String by extra
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

repositories {
    applyStandard()
}

val spine = Spine(project)

configurations {
    forceVersions()
    excludeProtobufLite()

    all {
        exclude("io.spine", "spine-validate")
        resolutionStrategy {
            force(
                "org.jetbrains.dokka:dokka-base:${Dokka.version}",
                Protobuf.compiler,
                spine.base,
                spine.validation.runtime,
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
    protoData(spine.validation.java)

    implementation(spine.base)
    implementation(spine.validation.runtime)

    testImplementation(JUnit.runner)
    testImplementation(spine.testlib)
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
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
            setFreeCompilerArgs()
        }
    }
}

/**
 * Suppress the "legacy" validation from McJava in favour of tha based on ProtoData.
 */
modelCompiler {
    // The below arrangement is "unusual" `java { }` because it conflicts with
    // `java` of type `JavaPluginExtension` in the `Project`.

    // Get nested `this` instead of `Project` instance.
    val mcOptions = (this@modelCompiler as ExtensionAware)
    val java = mcOptions.extensions.getByName("java") as McJavaOptions
    java.codegen {
        validation { skipValidation() }
    }
}

protoData {
    renderers(
        "io.spine.validation.java.PrintValidationInsertionPoints",
        "io.spine.validation.java.JavaValidationRenderer",

        // Suppress warnings in the generated code.
        "io.spine.protodata.codegen.java.file.PrintBeforePrimaryDeclaration",
        "io.spine.protodata.codegen.java.suppress.SuppressRenderer"

    )
    plugins(
        "io.spine.validation.ValidationPlugin",
    )
}

/**
 * Configure IntelliJ IDEA paths so that generated code is visible to the IDE.
 */
idea {
    module {
        val generatedDir = "$projectDir/generated"
        val generatedJavaDir = "$generatedDir/main/java"
        val generatedTestJavaDir = "$generatedDir/test/java"
        val generatedKotlinDir = "$generatedDir/main/kotlin"
        val generatedTestKotlinDir = "$generatedDir/test/kotlin"

        generatedSourceDirs.addAll(listOf(
            file(generatedJavaDir),
            file(generatedKotlinDir)
        ))
        testSources.from(
            file(generatedTestJavaDir),
            file(generatedTestKotlinDir),
        )
    }
}

updateGitHubPages(Spine.DefaultVersion.javadocTools) {
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

project.configureTaskDependencies()

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.generateReportIn(project)
LicenseReporter.mergeAllReports(project)
