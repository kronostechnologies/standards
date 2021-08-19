plugins {
    id("net.linguica.maven-settings") version "0.5"
}

subprojects {
    group = "com.equisoft.standards"

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
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        distributionSha256Sum = "a8da5b02437a60819cad23e10fc7e9cf32bcb57029d9cb277e26eeff76ce014b"
        gradleVersion = "7.2"
    }
}
