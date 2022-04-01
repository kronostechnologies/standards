package com.equisoft.standards.kotlin

import io.gitlab.arturbosch.detekt.CONFIGURATION_DETEKT_PLUGINS
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.resources.TextResource
import org.gradle.kotlin.dsl.dependencies
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

        configureDetekt()
        configureKotlinter()

        tasks.register("checkStatic") {
            staticChecks.forEach(::dependsOn)
        }

        tasks.named("check") {
            dependsOn("checkStatic")
        }
    }

    private fun Project.configureKotlinter() {
        extensions.configure(KotlinterExtension::class.java) {
            reporters = arrayOf("checkstyle", "plain", "sarif")
            disabledRules = arrayOf(
                "import-ordering"
            )
        }
    }

    private fun Project.configureDetekt() {
        addDetektFormattingPlugin()

        extensions.configure(DetektExtension::class.java) {
            val detektConfigInputStream: InputStream = getDetektConfigInputStream()
            val configuration = String(detektConfigInputStream.use(InputStream::readBytes))
            val configResource: TextResource = project.resources.text.fromString(configuration)

            buildUponDefaultConfig = true
            config = files(file(configResource))
            source = files(file("src/main/kotlin"), file("src/test/kotlin"))
        }

        tasks.withType(Detekt::class.java) {
            jvmTarget = JavaVersion.current().majorVersion
            reports {
                html.required.set(true)
                sarif.required.set(true)
            }
        }
    }

    private fun Project.addDetektFormattingPlugin() {
        val detektVersion = VersionsCatalog.get("detekt")
        dependencies {
            CONFIGURATION_DETEKT_PLUGINS("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
        }
    }

    private fun getDetektConfigInputStream(): InputStream {
        val detektConfigInputStream: InputStream? = this.javaClass.classLoader
            .getResourceAsStream(DETEKT_CONFIG_FILENAME)

        return checkNotNull(detektConfigInputStream) { "Detekt config file not found" }
    }

    companion object {
        private const val DETEKT_CONFIG_FILENAME = "detekt.yml"
    }
}
