import io.gitlab.arturbosch.detekt.CONFIGURATION_DETEKT_PLUGINS

val javaVersion = JavaVersion.VERSION_17
val detektVersion = "1.23.0"

plugins {
    `kotlin-dsl` apply false

    id("com.github.ben-manes.versions") version "0.47.0"
    id("org.cyclonedx.bom") version "1.7.4"
    id("io.gitlab.arturbosch.detekt") version "1.23.0" apply false
}

subprojects {
    group = "com.equisoft.standards"

    apply {
        plugin("org.cyclonedx.bom")
        plugin("org.gradle.java-gradle-plugin")
        plugin("org.gradle.kotlin.kotlin-dsl")
        plugin("org.gradle.maven-publish")
        plugin("io.gitlab.arturbosch.detekt")
    }

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
                    ?: System.getenv("GPR_KEY")
                        ?: System.getenv("GHCR_TOKEN")
            }
        }
    }

    dependencies {
        CONFIGURATION_DETEKT_PLUGINS("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    }

    configure<JavaPluginExtension> {
        withSourcesJar()

        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    configure<KotlinDslPluginOptions> {
        jvmTarget.set(javaVersion.majorVersion)
    }

    configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        buildUponDefaultConfig = true
        config = files("$rootDir/kotlin/src/main/resources/detekt.yml")
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
                        ?: System.getenv("GPR_KEY")
                                ?: System.getenv("GHCR_TOKEN")
                }
            }
        }
    }

    tasks {
        withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = javaVersion.majorVersion
                languageVersion = "1.6"
            }
        }

        withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
            jvmTarget = javaVersion.majorVersion
            reports {
                html.required.set(true)
                sarif.required.set(true)
            }
        }

        register("checkStatic") {
            group = "verification"
            dependsOn("detekt")
        }

        named("build") {
            dependsOn("checkStatic")
        }

        named("publish") {
            dependsOn("check", "checkStatic")
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
        gradleVersion = "8.1.1"
    }
}
