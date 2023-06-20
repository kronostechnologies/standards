package com.equisoft.standards.kotlin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Nested
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SuppressWarnings("FunctionNaming", "LargeClass")
class KotlinStandardsPluginFunctionalTest {
    private lateinit var projectDir: File
    private lateinit var sourcesDir: File
    private lateinit var testsDir: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    fun setUp() {
        projectDir = Files.createTempDirectory("kotlin-standards").toFile()
        createGradleFiles()

        sourcesDir = createProjectSubDirectory("src/main/kotlin")
        testsDir = createProjectSubDirectory("src/test/kotlin")

        // Create a file in each source directory to prevent task outcome of NO-SOURCE
        writeSource("Main.kt", "class Main")
        writeTest("MainTest.kt", "class MainTest")

        runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("--stacktrace")
    }

    private fun createGradleFiles() {
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText(
            """
                plugins {
                    kotlin("jvm") version "1.8.22"
                    id("com.equisoft.standards.kotlin")
                }
                dependencies {
                    implementation("io.micronaut.test:micronaut-test-junit5:3.5.0")
                    implementation("org.junit.jupiter:junit-jupiter-api:5.9.3")
                }
                repositories {
                    mavenCentral()
                }
            """.trimIndent()
        )
    }

    @AfterTest
    fun tearDown() {
        projectDir.deleteRecursively()
    }

    @Nested
    inner class Detekt {
        @Test
        fun `check should run detekt`() {
            val result = runner.build("check")

            assertDetektSuccess(result)
        }

        @Test
        fun `checkStatic should run detekt`() {
            val result = runner.build("checkStatic")

            assertDetektSuccess(result)
        }

        private fun assertDetektSuccess(result: BuildResult) {
            assertEquals(TaskOutcome.SUCCESS, result.task(":detektMain")?.outcome)
            assertEquals(TaskOutcome.SUCCESS, result.task(":detektTest")?.outcome)
        }
    }

    private fun writeSource(file: String, content: String) =
        sourcesDir.resolve(file).writeText(content + "\n")

    private fun writeTest(file: String, content: String) =
        testsDir.resolve(file).writeText(content + "\n")

    private fun createProjectSubDirectory(path: String): File = File(projectDir, path).apply { mkdirs() }

    private fun GradleRunner.appendArguments(vararg arguments: String): GradleRunner {
        return withArguments(getArguments() + arguments)
    }

    private fun GradleRunner.build(vararg arguments: String): BuildResult {
        return appendArguments(*arguments).build()
    }
}
