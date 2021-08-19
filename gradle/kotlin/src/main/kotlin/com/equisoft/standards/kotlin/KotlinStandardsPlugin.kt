package com.equisoft.standards.kotlin

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.jmailen.gradle.kotlinter.KotlinterExtension
import org.jmailen.gradle.kotlinter.KotlinterPlugin
import java.io.InputStream

class KotlinStandardsPlugin : Plugin<Project> {
    private val staticChecks = listOf(
        "lintKotlin",
        "detektMain",
        "detektTest",
    )

    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(KotlinterPlugin::class.java)
        plugins.apply(DetektPlugin::class.java)

        afterEvaluate {
            configureDetekt()
            configureKotlinter()
        }

        tasks.register("checkStatic") {
            staticChecks.forEach(::dependsOn)
        }

        tasks.named("check") {
            dependsOn("checkStatic")
        }

        tasks.withType(Detekt::class.java) {
            jvmTarget = "1.8"
        }
    }

    private fun Project.configureKotlinter() {
        extensions.configure(KotlinterExtension::class.java) {
            disabledRules = arrayOf(
                "import-ordering"
            )
        }
    }

    private fun Project.configureDetekt() {
        extensions.configure(DetektExtension::class.java) {
            val detektConfigInputStream: InputStream = getDetektConfigInputStream()
            val configuration = String(detektConfigInputStream.use(InputStream::readBytes))
            val configResource: TextResource = project.resources.text.fromString(configuration)

            config = files(file(configResource))
            input = files(file("src/main/kotlin"), file("src/test/kotlin"))
        }
    }

    private fun getDetektConfigInputStream(): InputStream {
        val detektConfigInputStream: InputStream? = this.javaClass.classLoader
            .getResourceAsStream(DETEKT_CONFIG_FILENAME)

        checkNotNull(detektConfigInputStream) { "Detekt config file not found" }

        return detektConfigInputStream
    }

    companion object {
        private const val DETEKT_CONFIG_FILENAME = "detekt.yml"
    }
}
