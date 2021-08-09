import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions")
    id("net.linguica.maven-settings")
}

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
