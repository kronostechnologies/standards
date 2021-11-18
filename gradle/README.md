# Gradle Plugins

A set of Gradle plugins.

## Plugin List

- `KotlinStandardsPlugin`:  
  Code style and static code analysis for Kotlin | [Documentation](./kotlin/README.md)
- `GlobalConventionsPlugin`:  
  Open source dependencies scanning (updates and known vulnerabilities) | [Documentation](./global-conventions/README.md)
- `OpenApiSdkPlugin`:  
  Configured OpenAPI Generator plugin | [Documentation](./openapi-sdk/README.md)

---

## Development

### Test locally in another project

1. In this project `./gradlew publishToMavenLocal`
1. In the other project's `settings.gradle`
    ```
    pluginManagement {
        repositories {
            mavenLocal {
                content {
                    includeGroupByRegex("com.equisoft\\..*")
                }
            }
            gradlePluginPortal()
        }
    }
    ```

### Publication

1. Make sure you have the `gpr.write.user` and `gpr.write.key` variables set in
   your `~/.gradle/gradle.properties` file. A PAT with the `write:packages` scope is required.
1. Bump the version
1. Build and publish
    ```bash
    cd the-plugin-to-publish
    make publish
    ```
