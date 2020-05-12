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

class KotlinStandardsPluginFunctionalTest {
    private lateinit var projectDir: File
    private lateinit var sourcesDir: File
    private lateinit var testsDir: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    fun setUp() {
        projectDir = Files.createTempDirectory("kotlin-standards").toFile()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                kotlin("jvm") version "1.3.71"
                id("com.equisoft.standards.kotlin")
            }
            repositories {
                jcenter()
            }
        """.trimIndent())

        sourcesDir = createProjectSubDirectory("src/main/kotlin")
        testsDir = createProjectSubDirectory("src/test/kotlin")

        // Create a file in each source directory to prevent task outcome of NO-SOURCE
        writeSource("Main.kt", "class Main")
        writeTest("MainTest.kt", "class MainTest")

        runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
    }

    @AfterTest
    fun tearDown() {
        projectDir.deleteRecursively()
    }

    @Nested
    inner class Kotlinter {
        @Test
        fun `check should run kotlinter`() {
            val result = runner.withArguments("check").build()

            assertKotlinterSuccess(result)
        }

        @Test
        fun `checkStatic should run kotlinter`() {
            val result = runner.withArguments("checkStatic").build()

            assertKotlinterSuccess(result)
        }

        @Test
        fun `kotlinter should ignore import-ordering`() {
            writeSource("IgnoreImportOrder.kt", """
            /* ktlint-disable no-unused-imports */
            import io.micronaut.test.annotation.MicronautTest
            import org.junit.jupiter.api.Assertions.assertEquals
            import org.junit.jupiter.api.Test
            import javax.inject.Inject

            class IgnoreImportOrder
        """.trimIndent())

            val result = runner.withArguments("checkStatic").build()

            assertKotlinterSuccess(result)
        }

        private fun assertKotlinterSuccess(result: BuildResult) {
            assertEquals(TaskOutcome.SUCCESS, result.task(":lintKotlinMain")?.outcome, ":lintKotlinMain")
            assertEquals(TaskOutcome.SUCCESS, result.task(":lintKotlinTest")?.outcome, ":lintKotlinTest")
            assertEquals(TaskOutcome.SUCCESS, result.task(":lintKotlin")?.outcome, ":lintKotlin")
        }
    }

    @Nested
    inner class Detekt {
        @Test
        fun `check should run detekt`() {
            val result = runner.withArguments("check").build()

            assertDetektSuccess(result)
        }

        @Test
        fun `checkStatic should run detekt`() {
            val result = runner.withArguments("checkStatic").build()

            assertDetektSuccess(result)
        }

        private fun assertDetektSuccess(result: BuildResult) {
            assertEquals(TaskOutcome.SUCCESS, result.task(":detekt")?.outcome)
        }
    }

    private fun writeSource(file: String, content: String) =
        sourcesDir.resolve(file).writeText(content + "\n")

    private fun writeTest(file: String, content: String) =
        testsDir.resolve(file).writeText(content + "\n")

    private fun createProjectSubDirectory(path: String): File = File(projectDir, path).apply { mkdirs() }
}
