/*
<<<<<<< HEAD
 * Copyright 2021, TeamDev. All rights reserved.
=======
 * Copyright 2020, TeamDev. All rights reserved.
>>>>>>> 4e922c830321ebec6586f7e846a97e8dd5765181
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

<<<<<<< HEAD
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.remove
import io.spine.internal.dependency.CheckerFramework
import io.spine.internal.dependency.ErrorProne
import io.spine.internal.dependency.Flogger
import io.spine.internal.dependency.Guava
import io.spine.internal.dependency.JUnit
import io.spine.internal.dependency.JavaX
import io.spine.internal.dependency.Protobuf
import io.spine.internal.gradle.PublishingRepos
import io.spine.internal.gradle.Scripts
import io.spine.internal.gradle.applyStandard
import io.spine.internal.gradle.excludeProtobufLite
import io.spine.internal.gradle.forceVersions
import io.spine.internal.gradle.spinePublishing
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("RemoveRedundantQualifierName") // cannot use imports under `buildScript`
buildscript {
    apply(from = "$projectDir/../version.gradle.kts")
    val spineVersion: String by extra

    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        io.spine.internal.dependency.Protobuf.libs.forEach {
            classpath(it)
        }

        classpath(io.spine.internal.dependency.Guava.lib)
        classpath(io.spine.internal.dependency.Flogger.lib)
        classpath(io.spine.internal.dependency.CheckerFramework.annotations)

        io.spine.internal.dependency.ErrorProne.annotations.forEach {
            classpath(it)
        }

        classpath(io.spine.internal.dependency.JavaX.annotations)
        classpath(io.spine.internal.dependency.Protobuf.GradlePlugin.lib)

        classpath(io.spine.internal.dependency.JavaPoet.lib)
        classpath(io.spine.internal.dependency.Flogger.Runtime.systemBackend)

        // A library for parsing Java sources.
        // Used for parsing Java sources generated from Protobuf files
        // to make their annotation more convenient.
        with(io.spine.internal.dependency.Roaster) {
            classpath(api) {
                exclude(group = "com.google.guava")
            }
            classpath(jdt) {
                exclude(group = "com.google.guava")
            }
        }
        classpath("io.spine.tools:spine-mc-java:$spineVersion")
=======
@file:Suppress("RemoveRedundantQualifierName") // To prevent IDEA replacing FQN imports.

import io.spine.gradle.internal.DependencyResolution
import io.spine.gradle.internal.Deps
import io.spine.gradle.internal.PublishingRepos

buildscript {
    apply(from = "version.gradle.kts")
    apply(from = "$rootDir/config/gradle/dependencies.gradle")

    val dependencyResolution = io.spine.gradle.internal.DependencyResolution

    val spineBaseVersion: String by extra
    val spineTimeVersion: String by extra

    dependencyResolution.defaultRepositories(repositories)
    dependencyResolution.forceConfiguration(configurations)

    configurations.all {
        resolutionStrategy {
            force(
                    "io.spine:spine-base:$spineBaseVersion",
                    "io.spine:spine-time:$spineTimeVersion"
            )
        }
>>>>>>> 4e922c830321ebec6586f7e846a97e8dd5765181
    }
}

plugins {
    `java-library`
<<<<<<< HEAD
    kotlin("jvm") version io.spine.internal.dependency.Kotlin.version
    idea
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.Protobuf.GradlePlugin.apply {
        id(id) version version
    }
    @Suppress("RemoveRedundantQualifierName") // Cannot use imports here.
    io.spine.internal.dependency.ErrorProne.GradlePlugin.apply {
        id(id) version version
    }
    pmd
    jacoco
    `force-jacoco`
    `project-report`
    `pmd-settings`
}

apply(plugin = "io.spine.mc-java")

apply(from = "$projectDir/../version.gradle.kts")
val spineVersion: String by extra
val versionToPublish: String by extra

group = "io.spine"
version = versionToPublish

spinePublishing {
    targetRepositories.addAll(setOf(
        PublishingRepos.cloudRepo,
        PublishingRepos.gitHub("base")
    ))
    publish(project)
}

val javaVersion = JavaVersion.VERSION_1_8

the<JavaPluginExtension>().apply {
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
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

repositories.applyStandard()
configurations.forceVersions()
configurations.excludeProtobufLite()

// The dependencies should be similar to those defined in the `../build.gradle.kts`.
dependencies {
    errorprone(ErrorProne.core)
    errorproneJavac(ErrorProne.javacPlugin)

    Protobuf.libs.forEach { api(it) }
    api(Flogger.lib)
    api(Guava.lib)
    api(CheckerFramework.annotations)
    api(JavaX.annotations)
    ErrorProne.annotations.forEach { api(it) }
    api(kotlin("stdlib-jdk8"))
    api("io.spine:spine-base:$spineVersion")

    testImplementation(JUnit.runner)
    testImplementation(JUnit.pioneer)
    testImplementation("io.spine.tools:spine-testlib:$spineVersion")
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

tasks.withType<JavaCompile> {
    val currentJavaVersion = JavaVersion.current()
    if (currentJavaVersion != JavaVersion.VERSION_1_8) {
        throw GradleException(
            "Base types must be built using Java 8 (as the main project)." +
                    " The version of Java in this project: $currentJavaVersion."
        )
    }

    // Explicitly sets the encoding of the source and test source files, ensuring
    // correct execution of the `javac` task.
    options.encoding = "UTF-8"
}

apply {
    with(Scripts) {
        from(javadocOptions(project))
        from(javacArgs(project))
        from(updateGitHubPages(project))
    }
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
=======
    idea
    id("com.google.protobuf").version(io.spine.gradle.internal.Deps.versions.protobufPlugin)
    id("net.ltgt.errorprone").version(io.spine.gradle.internal.Deps.versions.errorPronePlugin)
    id("io.spine.tools.gradle.bootstrap") version "1.7.0" apply false
}

apply(from = "version.gradle.kts")
val spineCoreVersion: String by extra
val spineBaseVersion: String by extra
val spineTimeVersion: String by extra

extra["projectsToPublish"] = listOf(
        "template-client",
        "template-server"
)
extra["credentialsPropertyFile"] = PublishingRepos.cloudRepo.credentials

allprojects {
    apply {
        plugin("jacoco")
        plugin("idea")
        plugin("project-report")
        apply(from = "$rootDir/version.gradle.kts")
    }

    group = "io.spine.template"
    version = extra["versionToPublish"]!!
}

subprojects {
    apply {
        plugin("java-library")
        plugin("net.ltgt.errorprone")
        plugin("pmd")
        plugin("io.spine.tools.gradle.bootstrap")

        from(Deps.scripts.javacArgs(project))
        from(Deps.scripts.pmd(project))
        from(Deps.scripts.projectLicenseReport(project))
        from(Deps.scripts.testOutput(project))
        from(Deps.scripts.javadocOptions(project))

        from(Deps.scripts.testArtifacts(project))
    }

    val isTravis = System.getenv("TRAVIS") == "true"
    if (isTravis) {
        tasks.javadoc {
            val opt = options
            if (opt is CoreJavadocOptions) {
                opt.addStringOption("Xmaxwarns", "1")
            }
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    DependencyResolution.defaultRepositories(repositories)

    dependencies {
        errorprone(Deps.build.errorProneCore)
        errorproneJavac(Deps.build.errorProneJavac)

        implementation(Deps.build.guava)
        compileOnlyApi(Deps.build.jsr305Annotations)
        compileOnlyApi(Deps.build.checkerAnnotations)
        Deps.build.errorProneAnnotations.forEach { compileOnlyApi(it) }

        testImplementation(Deps.test.guavaTestlib)
        Deps.test.junit5Api.forEach { testImplementation(it) }
        Deps.test.truth.forEach { testImplementation(it) }
        testImplementation("io.spine.tools:spine-mute-logging:$spineBaseVersion")

        testRuntimeOnly(Deps.test.junit5Runner)
    }

    DependencyResolution.forceConfiguration(configurations)
    configurations {
        all {
            resolutionStrategy {
                force(
                        "io.spine:spine-base:$spineBaseVersion",
                        "io.spine:spine-testlib:$spineBaseVersion",
                        "io.spine:spine-base:$spineBaseVersion",
                        "io.spine:spine-time:$spineTimeVersion"
                )
            }
        }
    }
    DependencyResolution.excludeProtobufLite(configurations)

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    apply {
        from(Deps.scripts.slowTests(project))
        from(Deps.scripts.testOutput(project))
        from(Deps.scripts.javadocOptions(project))
    }

    tasks.register("sourceJar", Jar::class) {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register("testOutputJar", Jar::class) {
        from(sourceSets.test.get().output)
        archiveClassifier.set("test")
    }

    tasks.register("javadocJar", Jar::class) {
        from("$projectDir/build/docs/javadoc")
        archiveClassifier.set("javadoc")

        dependsOn(tasks.javadoc)
    }
}

apply {
    from(Deps.scripts.publish(project))

    // Aggregated coverage report across all subprojects.
    from(Deps.scripts.jacoco(project))

    // Generate a repository-wide report of 3rd-party dependencies and their licenses.
    from(Deps.scripts.repoLicenseReport(project))

    // Generate a `pom.xml` file containing first-level dependency of all projects in the repository.
    from(Deps.scripts.generatePom(project))
>>>>>>> 4e922c830321ebec6586f7e846a97e8dd5765181
}
