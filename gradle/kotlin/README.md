# Equisoft's Kotlin Standards

This project is a plugin to validate Equisoft's Kotlin code style. The plugin
wraps [Detekt](https://detekt.github.io/detekt/) and [Kotlinter](https://github.com/jeremymailen/kotlinter-gradle) to
validate the code style.

## Installation

> Note: GitHub Packages require authentication via a [PAT](https://github.com/settings/tokens/new?description=GPR%20read-only&scopes=read:packages) to access all packages, even public one. This is likely to change in the future, but nobody knows when.

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
    id("com.equisoft.standards.kotlin") version "0.5.0"
}
```

## Usage

The code style validations can now be performed with `./gradlew check` or `./gradlew checkStatic`.

## Continuous Integration

We strongly suggest that you enforce code style checks on your CI. For example, on CircleCI you can add a configuration
similar to this one to your _.circleci/config.yml_:

```yaml
orbs:
  eq: equisoft/build-tools@latest
  gradle: 'circleci/gradle@latest'

jobs:
  checkStatic:
    executor: java
    steps:
      - eq/with-gradle-cache:
          steps:
            - eq/gradlew:
                title: Static checks
                task: checkStatic
      - gradle/collect_test_results
```
