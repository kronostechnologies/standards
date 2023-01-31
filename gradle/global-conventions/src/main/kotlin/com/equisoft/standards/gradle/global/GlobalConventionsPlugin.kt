package com.equisoft.standards.gradle.global

import com.github.benmanes.gradle.versions.VersionsPlugin
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.cyclonedx.gradle.CycloneDxPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.repositories

@Suppress("ComplexMethod", "LongMethod")
class GlobalConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(VersionsPlugin::class.java)
        plugins.apply(CycloneDxPlugin::class.java)

        repositories {
            mavenCentral()
            configureGitHubRepository(project, "kronostechnologies")
            configureGitHubRepository(project, "equisoft")
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

    private fun RepositoryHandler.configureGitHubRepository(
        project: Project,
        organisation: String
    ): MavenArtifactRepository =
        maven("https://maven.pkg.github.com/$organisation/*/") {
            credentials {
                username = project.findProperty("gpr.user")?.toString()
                    ?: System.getenv("GPR_USER")
                    ?: System.getenv("GHCR_USER")
                password = project.findProperty("gpr.key")?.toString()
                    ?: System.getenv("GPR_KEY")
                    ?: System.getenv("GHCR_TOKEN")
            }
        }
}
