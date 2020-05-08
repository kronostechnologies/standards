package com.equisoft.standards.kotlin

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinStandardsPluginFunctionalTest {
    private lateinit var projectDir: File
    private lateinit var runner: GradleRunner

    @BeforeTest
    fun setUp() {
        projectDir = File("build/functionalTest")
        projectDir.mkdirs()
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

        runner = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
    }

    @Test
    fun `check should run kotlinter`() {
        val result = runner.withArguments("check").build()

        assertOutputContainsKotlinter(result)
    }

    @Test
    fun `checkStatic should run kotlinter`() {
        val result = runner.withArguments("checkStatic").build()

        assertOutputContainsKotlinter(result)
    }

    private fun assertOutputContainsKotlinter(result: BuildResult) {
        assertTrue(result.output.contains("> Task :lintKotlinMain"))
        assertTrue(result.output.contains("> Task :lintKotlinTest"))
        assertTrue(result.output.contains("> Task :lintKotlin"))
    }

    @Test
    fun `check should run detekt`() {
        val result = runner.withArguments("check").build()

        assertOutputContainsDetekt(result)
    }

    @Test
    fun `checkStatic should run detekt`() {
        val result = runner.withArguments("checkStatic").build()

        assertOutputContainsDetekt(result)
    }

    private fun assertOutputContainsDetekt(result: BuildResult) {
        assertTrue(result.output.contains("> Task :detekt"))
    }
}
