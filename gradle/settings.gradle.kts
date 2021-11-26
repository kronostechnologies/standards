rootProject.name = "gradle-plugins"

include(
    "global-conventions",
    "kotlin",
    "openapi-sdk",
)

val isCiServer = System.getenv().containsKey("CI")

buildCache {
    local {
        isEnabled = !isCiServer
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kronostechnologies/*/") {
            name = "github"
            credentials {
                username =
                    if (extra.has("gpr.user")) extra["gpr.user"].toString()
                    else System.getenv("GPR_USER") ?: System.getenv("GHCR_USER")
                password =
                    if (extra.has("gpr.key")) extra["gpr.key"]?.toString()
                    else System.getenv("GPR_TOKEN") ?: System.getenv("GHCR_TOKEN")
            }
        }
    }
}
