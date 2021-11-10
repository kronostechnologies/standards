package com.equisoft.standards.gradle.global

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import net.linguica.gradle.maven.settings.MavenSettingsPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.repositories
import org.owasp.dependencycheck.gradle.DependencyCheckPlugin
import org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension
import org.owasp.dependencycheck.reporting.ReportGenerator.Format

@Suppress("LongMethod")
class GlobalConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(VersionsPlugin::class.java)
        plugins.apply(MavenSettingsPlugin::class.java)
        plugins.apply(DependencyCheckPlugin::class.java)

        repositories {
            mavenCentral()
            maven("https://maven.pkg.github.com/kronostechnologies/*/") {
                name = "github"
                credentials {
                    username = project.findProperty("gpr.user")?.toString()
                        ?: System.getenv("GPR_USER")
                        ?: System.getenv("GHCR_USER")
                    password = project.findProperty("gpr.key")?.toString()
                        ?: System.getenv("GPR_TOKEN")
                        ?: System.getenv("GHCR_TOKEN")
                }
            }
        }

        afterEvaluate {
            extensions.configure(DependencyCheckExtension::class.java) {
                formats = listOf(Format.SARIF, Format.HTML)

                suppressionFiles = listOf(
                    OWASP_COMMON_SUPPRESSIONS_FILE,
                    "owasp-suppressions.xml"
                )
            }
        }

        tasks {
            named<DependencyUpdatesTask>("dependencyUpdates").configure {
                fun isStable(version: String): Boolean {
                    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
                    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
                    return stableKeyword || regex.matches(version)
                }

                checkConstraints = true
                gradleReleaseChannel = "current"
                outputFormatter = "json,html"

                rejectVersionIf {
                    !isStable(candidate.version) && isStable(currentVersion)
                }
            }
        }
    }

    companion object {
        private const val OWASP_COMMON_SUPPRESSIONS_FILE =
            "https://raw.githubusercontent.com/kronostechnologies/standards/master/gradle/owasp-suppressions.xml"
    }
}
