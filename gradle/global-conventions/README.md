# Equisoft's Global Conventions

This project is a plugin that wraps
[gradle-versions-plugin](https://github.com/ben-manes/gradle-versions-plugin) and
OWASP's [DependencyCheck](https://github.com/jeremylong/DependencyCheck).

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
                password = extra["gpr.key"]?.toString() ?: System.getenv("GPR_TOKEN") ?: System.getenv("GHCR_TOKEN")
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

Make sure to create this file at the root of your gradle project:

*owasp-suppressions.xml*

```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    <!-- https://jeremylong.github.io/DependencyCheck/general/suppression.html -->
</suppressions>
```

This is the `DependencyCheck`'s suppressions file for the false positives. Global suppressions are also
configured [here](https://github.com/kronostechnologies/standards/blob/master/gradle/owasp-suppressions.xml)

## Usage

- Scan dependencies for known vulnerabilities: `./gradlew dependencyCheckAggregate`
- List outdated dependencies: `./gradlew dependencyUpdates`

## Continuous Integration

TODO
