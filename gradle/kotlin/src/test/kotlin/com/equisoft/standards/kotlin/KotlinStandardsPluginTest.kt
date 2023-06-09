package com.equisoft.standards.kotlin

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class KotlinStandardsPluginTest {
    private lateinit var project: Project

    @BeforeTest
    fun setUp() {
        project = ProjectBuilder.builder().build()
        project.plugins.apply("application")
        project.plugins.apply("com.equisoft.standards.kotlin")
    }

    @Test
    fun `detekt plugin should be registered`() {
        assertTrue(project.plugins.hasPlugin("io.gitlab.arturbosch.detekt"))
    }
}
