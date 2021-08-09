group = "com.equisoft.standards"
version = "0.0.1"

plugins {
    `kotlin-dsl`
    `maven-publish`

    id("net.linguica.maven-settings") version "0.5"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("net.linguica.gradle:maven-settings-plugin:0.5")
}

publishing {
    repositories {
        maven {
            name = "equisoft-standards"
            url = uri("https://maven.pkg.github.com/kronostechnologies/standards")
            credentials {
                name = "gprWrite"
                username = project.findProperty("gpr.user")?.toString()
                    ?: System.getenv("GPR_USER")
                    ?: System.getenv("GHCR_USER")
                password = project.findProperty("gpr.key")?.toString()
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GHCR_TOKEN")
            }
        }
    }
}

tasks {
    publish {
        dependsOn("check")
    }
}
