# Equisoft's Micronaut Gradle plugin

This plugins applies and configure
the [official Micronaut plugin](https://github.com/micronaut-projects/micronaut-gradle-plugin).

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
    id("com.equisoft.micronaut") version "0.1.0"
}
```

## Usage

```
micronautSettings {
    projectRootPackage.set("my.root.package")
}
```

### Options

`projectRootPackage`: Root package of the application  
`javaVersion` (optional): JavaVersion to use (default `JavaVersion.current()`)  
`exposedPorts` (optional): Ports to expose in the Dockerfile (default `8080, 8081`)  
`runtimeImage` (optional): Image to use as runtime in the Native Dockerfile (
default `gcr.io/distroless/cc-debian11:nonroot`)  
