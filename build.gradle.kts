/*
 * Copyright 2023, TeamDev. All rights reserved.
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
import io.spine.internal.dependency.Validation
import io.spine.internal.gradle.VersionWriter
import io.spine.internal.gradle.checkstyle.CheckStyleConfig
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.javadoc.JavadocConfig
import io.spine.internal.gradle.publish.IncrementGuard
import io.spine.internal.gradle.publish.PublishingRepos
import io.spine.internal.gradle.publish.PublishingRepos.gitHub
import io.spine.internal.gradle.publish.spinePublishing
import io.spine.internal.gradle.report.license.LicenseReporter
import io.spine.internal.gradle.report.pom.PomGenerator
import io.spine.internal.gradle.standardToSpineSdk
import io.spine.tools.mc.gradle.modelCompiler
import io.spine.tools.mc.java.gradle.McJavaOptions
import org.gradle.jvm.tasks.Jar

buildscript {
    apply(from = "$projectDir/version.gradle.kts")
    standardSpineSdkRepositories()
    repositories {
        io.spine.internal.gradle.publish.PublishingRepos.gitHub("mc-java")
    }

    dependencies {
        classpath(io.spine.internal.dependency.Spine.McJava.pluginLib)
    }

    configurations {
        all {
            resolutionStrategy {
                force(
                    io.spine.internal.dependency.Dokka.BasePlugin.lib,
                    io.spine.internal.dependency.Spine.base,
                )
            }
        }
    }
}

plugins {
    `jvm-module`
    protobuf
    protodata
}

// Cannot use `id()` syntax for McJava because it's not yet published to the Plugin Portal
// and is added to the build classpath via `buildScript` block above.
apply(plugin = "io.spine.mc-java")

apply<IncrementGuard>()
apply<VersionWriter>()

apply(from = "$projectDir/version.gradle.kts")
val baseVersion: String by extra
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
        exclude("io.spine", "spine-validate")
        resolutionStrategy {
            force(
                "org.jetbrains.dokka:dokka-base:${Dokka.version}",
                Protobuf.compiler,
                Spine.base,
                Spine.toolBase,
                Validation.runtime,
                JUnit.runner,
            )
        }
    }
}

dependencies {
    errorprone(ErrorProne.core)
    protoData(Validation.java)

    implementation(Spine.base)
    implementation(Validation.runtime)

    testImplementation(JUnit.runner)
    testImplementation(Spine.testlib)
}

spinePublishing {
    destinations = setOf(
        gitHub("base-types"),
        PublishingRepos.cloudRepo,
        PublishingRepos.cloudArtifactRegistry
    )

    dokkaJar {
        kotlin = true
        java = true
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
        "io.spine.protodata.codegen.java.annotation.SuppressWarningsAnnotation"

    )
    plugins(
        "io.spine.validation.ValidationPlugin",
    )
}

/**
 * Forcibly remove the code by `protoc` under the `build` directory
 * until ProtoData can handle it by itself.
 *
 * The generated code is transferred by ProtoData into `$projectDir/generated` while it
 * processes it. But it does not handle the compilation task input yet.
 *
 * Therefore [compileJava] and [compileKotlin] tasks below depend on this removal task
 * to avoid duplicated declarations error during the compilation.
 */
val removeGeneratedVanillaCode by tasks.registering(Delete::class) {
    delete("$buildDir/generated/source/proto")
}

val compileJava: Task by tasks.getting {
    dependsOn(removeGeneratedVanillaCode)
}

val compileKotlin: Task by tasks.getting {
    dependsOn(removeGeneratedVanillaCode)
}

//tasks {
//    jacocoTestReport {
//        dependsOn(test)
//        reports {
//            xml.required.set(true)
//        }
//    }
//    test {
//        finalizedBy(jacocoTestReport)
//    }
//}

/**
 * Handle potential duplication of source code files in `sourcesJar` tasks.
 */
tasks {
    withType<Jar>().configureEach {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

project.configureTaskDependencies()

CheckStyleConfig.applyTo(project)
JavadocConfig.applyTo(project)
PomGenerator.applyTo(project)
LicenseReporter.generateReportIn(project)
LicenseReporter.mergeAllReports(project)
