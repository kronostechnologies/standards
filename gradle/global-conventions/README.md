# Equisoft's Global Conventions

This project is a plugin that wraps :
- [gradle-versions-plugin](https://github.com/ben-manes/gradle-versions-plugin)
- [cyclonedx-gradle-plugin](https://github.com/CycloneDX/cyclonedx-gradle-plugin)

## Installation

Because the plugin is hosted on GitHub Packages, it is necessary to configure the repository.

> Note: GitHub Packages require authentication via a
> [PAT](https://github.com/settings/tokens/new?description=GPR%20read-only&scopes=read:packages) to access all
> packages, even public one. This is likely to change in the future, but nobody knows when.

*settings.gradle.kts*

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.github.com/kronostechnologies/*/") {
            name = "github"
            credentials {
                username = extra["gpr.user"]?.toString() ?: System.getenv("GPR_USER") ?: System.getenv("GHCR_USER")
                password = extra["gpr.key"]?.toString() ?: System.getenv("GPR_KEY") ?: System.getenv("GHCR_TOKEN")
            }
        }
    }
}
```

*build.gradle.kts*

```kotlin
plugins {
    id("com.equisoft.standards.global-conventions") version "0.0.0"
}
```

## Usage

- List outdated dependencies: `./gradlew dependencyUpdates`
- Generate CycloneDX BOM : `./gradlew cyclonedxBom`

## Continuous Integration

TODO
