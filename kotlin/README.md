# Kotlin Standards

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
