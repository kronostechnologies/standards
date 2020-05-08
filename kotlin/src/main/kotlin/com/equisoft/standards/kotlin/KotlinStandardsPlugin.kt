package com.equisoft.standards.kotlin

import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.jmailen.gradle.kotlinter.KotlinterPlugin
import java.io.InputStream

class KotlinStandardsPlugin : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        plugins.apply(KotlinterPlugin::class.java)
        plugins.apply(DetektPlugin::class.java)

        afterEvaluate {
            convention.configure(DetektExtension::class.java) {
                val detektConfigInputStream: InputStream = getDetektConfigInputStream()
                val configuration = String(detektConfigInputStream.use(InputStream::readBytes))
                val configResource: TextResource = project.resources.text.fromString(configuration)

                it.config = files(file(configResource))
                it.input = files(file("src/main/kotlin"), file("src/test/kotlin"))
            }
        }

        tasks.register("checkStatic").configure { check ->
            check.dependsOn("lintKotlin")
            check.dependsOn("detekt")
        }
    }

    private fun getDetektConfigInputStream(): InputStream {
        val detektConfigInputStream: InputStream? = this.javaClass.classLoader
                .getResourceAsStream(DETEKT_CONFIG_FILENAME)

        checkNotNull(detektConfigInputStream, { "Detekt config file not found" })

        return detektConfigInputStream
    }

    companion object {
        private const val DETEKT_CONFIG_FILENAME = "detekt.yml"
    }
}
