plugins {
    `kotlin-dsl` apply false // It needs to be imported first by the root project for some obscure Gradle-ish reason

    id("net.linguica.maven-settings") version "0.5"
    id("com.github.ben-manes.versions") version "0.39.0"
}

subprojects {
    group = "com.equisoft.standards"

    apply(plugin = "org.gradle.java-gradle-plugin")
    apply(plugin = "org.gradle.kotlin.kotlin-dsl")
    apply(plugin = "org.gradle.maven-publish")
    apply(plugin = "net.linguica.maven-settings")

    repositories {
        gradlePluginPortal()
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

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "equisoft-standards"
                url = uri("https://maven.pkg.github.com/kronostechnologies/standards")
                credentials {
                    name = "gprWrite"
                    username = project.findProperty("gpr.write.user")?.toString()
                        ?: System.getenv("GPR_USER")
                            ?: System.getenv("GHCR_USER")
                    password = project.findProperty("gpr.write.key")?.toString()
                        ?: System.getenv("GPR_TOKEN")
                            ?: System.getenv("GHCR_TOKEN")
                }
            }
        }
    }

    tasks {
        named("publish") {
            dependsOn("check")
        }
    }
}

tasks {
    dependencyUpdates {
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

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        distributionSha256Sum = "a8da5b02437a60819cad23e10fc7e9cf32bcb57029d9cb277e26eeff76ce014b"
        gradleVersion = "7.2"
    }
}
