# Equisoft's Kotlin Standards

This project is a plugin to validate Equisoft's Kotlin code style. The plugin
wraps [Detekt](https://detekt.github.io/detekt/) and [Kotlinter](https://github.com/jeremymailen/kotlinter-gradle) to
validate the code style.

## Installation

Available on the Gradle Plugins
Portal: [com.equisoft.standards.kotlin](https://plugins.gradle.org/plugin/com.equisoft.standards.kotlin)

Add the plugin to your project's `gradle.kts` configuration:

```kotlin
plugins {
    id("com.equisoft.standards.kotlin") version "0.2.0"
}
```

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

## Test locally in another project

1. In this project `./gradlew publishToMavenLocal`
1. In the other project's `settings.gradle`
    ```
    pluginManagement {
        repositories {
            mavenLocal() {
                content {
                    includeGroupByRegex("com.equisoft\\..*")
                }
            }
            gradlePluginPortal()
        }
    }
    ```

## Publication

1. Make sure you have the `gradle.publish.key` and `gradle.publish.secret` variables set in
   your `~/.gradle/gradle.properties` file. They are available on LastPass.
1. Bump the version
1. Build and publish
    ```bash
    make publish
    ```
