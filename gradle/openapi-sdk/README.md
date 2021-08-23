# OpenAPI SDK Gradle Plugin

This plugin is a _very opinionated_ pre-configuration of
the [OpenAPI Generator plugin](https://github.com/OpenAPITools/openapi-generator/) for Gradle. Its goal is to help us
remove a lot of boilerplate from the build scripts across our various repositories.

It does two things:

1. If Micronaut and Kotlin Kapt are available, it configures how the openapi.yaml specification is generated.
2. It registers tasks to generate "SDK" projects. Those are basically OpenAPI generated projects that are then pushed to
   a repository and published as a library.

## Usage

At the bare minimum, the plugin needs to be imported and told where the openapi.yaml file is located. Because the plugin
is hosted on GitHub Packages, it is also necessary to configure the repository.

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
                password = extra["gpr.key"]?.toString() ?: System.getenv("GPR_TOKEN") ?: System.getenv("GHCR_TOKEN")
            }
        }
    }
}
```

*build.gradle.kts*

```kotlin
plugins {
    id("com.equisoft.standards.openapi-sdk") version "0.0.0"
}

openApiSdk {
    specFile.set(file("$buildDir/tmp/kapt3/classes/main/META-INF/swagger/name-$version.yml"))
    
    git {
        enable.set(true)
        userId.set("kronostechnologies")
    }
}
```

## Features

### OpenAPI specification generation

The generation of the OpenAPI spec file is mostly without any configurations.

The plugin will detect if the micronaut-bom is part of the project's dependencies and automatically add the dependencies
and configurations required. You can remove `swagger-annotations` and `micronaut-openapi` as they are handled by the
plugin.

A `generateOpenapiProperties` task will be registered. This task will generate a properties file to configure how
Micronaut generates the specification. Configurations are added for Kotlin Kapt to read this file and add Kapt as a
dependency to other compilation and validation tasks.

It is possible to add new configurations to the OpenAPI properties:

```kotlin
tasks {
    generateOpenapiProperties {
        property("micronaut.openapi.expand.my.custom.property", "Can contain whatever I want")
    }
}
```

The Micronaut documentation contains more details about this
file: https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html#propertiesFileConfiguration

### SDK generation

The SDK generation creates various tasks to assemble, test and even synchronize projects generated from the provided
OpenAPI specification.

It is not required to use the "OpenAPI specification generation" feature to generate SDKs. If the OpenAPI spec
generation is not run, then the `specFile` property must point to a valid file. The configurations are centered around the following OpenAPI generators:

- [Kotlin](https://openapi-generator.tech/docs/generators/kotlin/)
- [Micronaut](https://github.com/kronostechnologies/micronaut-project-openapi-generator)
- [PHP](https://openapi-generator.tech/docs/generators/php/)
- [Typescript-fetch](https://openapi-generator.tech/docs/generators/typescript-fetch/)

For each one of these generators, multiple tasks are generated. Using Kotlin as an example, here are the tasks that will
be available:

| Task | Type | Action |
| --- | --- | --- |
| `syncKotlinSdk` | [SyncRepositoryTask](. "com.equisoft.standards.gradle.openapisdk.tasks.SyncRepositoryTask") | *Only with git enabled.* Clones or updates the kotlin SDK repository for this project. |
| `assembleKotlinSdk` | [GenerateTask](. "org.openapitools.generator.gradle.plugin.tasks.GenerateTask") | Generates the SDK project code and files. |
| `checkKotlinSdk` | [CheckSdkTask](. "com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask") | Compiles the generated project and/or executes its tests. Depends on the language and the features supported by the underlying generator. |
| `buildKotlinSdk` | N/D | `assembleKotlinSdk` + `checkKotlinSdk` |
| `publishKotlinSdk` | [PublishSdkTask](. "com.equisoft.standards.gradle.openapisdk.tasks.PublishSdkTask") | Prints a code fence describing the publish process. In the future this may perform the publication automatically. |

> While it is possible to alter the different tasks, it is recommended to use the defaults to facilitate maintenance and promote consistency across our plethora of SDK projects.

### SDK repository synchronization

The SDK generation can synchronize the code with a remote GIT repository. This feature is disabled by default. It can be enabled by configuring the git property of the `openApiSdk` extension in the **build.gradle** file:

```kotlin
openApiSdk {
    git {
        enable.set(true)
        userId.set("kronostechnologies") // The organization or username in the repository path
    }
}
```

When enabled, the `sync` task will be executed before the code is generated. The plugin attempts to set a sensible
default for the GIT URI: `git@${gitHost}:${gitUserId}/${repoId}.git` where `repoId = ${projectKey}-sdk-${generatorName}`
and `projectKey = rootProject.name`.

For example, the current repository would use the following
URI: `git@github.com:kronostechnologies/gradle-plugins-sdk-kotlin.git`.

Many of these variables can be modified. If a specific SDK uses a non-default URI, the `sync` task can be altered:

```kotlin
openApiSdk {
    projectKey.set("my-custom-project")
    git {
        enable.set(true)
        host.set("bitbucket.org")
        userId.set("equisoft")
    }
}

tasks {
    // A sync task can be reconfigured if a SDK is hosted on a different repo than the defaults. 
    syncPhpSdk {
        host.set("github.com")
        userId.set("kronostechnologies")
        repoId.set("connect-sdk-php8")
    }
}

```

## Configuration options

Here's a complete list of the plugin extension properties:

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| openApiPropertiesFile | File | `./build/openapi.properties` | The file to write the OpenAPI generation configs to.
| outputDir | Directory | `./build/sdk` | The directory where the SDK projects are generated. Each SDK is generated in a sub-directory under this path.
| projectKey | String | `rootProject.name` | Used in various places to guess the name of the generated projects.
| specFile | File | - | The OpenAPI yaml specification. Used by the spec generator as an output and by the SDK generators as an input. 
| swaggerVersion | File | - | Use this specific version for the `swagger-annotations` dependency. Otherwise uses the version configured by `micronaut-bom`.
| git.enable | Boolean | `false` | Enable the sync and publish SDK tasks.
| git.host | String | `github.com` | The GIT host used to generate the repository URI.
| git.userId | String | - | The GIT organization or user name used to generate the repository URI.

## Patches

Rather than monkey patching the templates, the build process will apply patches stored in `/src/patches`. There are a couple restrictions:

- The patches must be in the same directory structure than the OpenAPI generator.
- The name of the patch file must be the same as the OpenAPI template, followed by the `.patch` extension.
- Only one file per diff file is possible.

### Create a new patch 

To create a new patch, you can follow this procedure:

1. Copy OpenAPI templates locally to `build/tmp/openapi-templates`:
    ```shell
    ./gradlew :openapi-sdk:syncTemplates
    ```
1. Make a second copy of the files to be patched in `build/resources/patches`:
    ```shell
    cp build/tmp/openapi-templates/kotlin-client/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache \
       build/resources/patches/kotlin-client/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache
    ```
1. Modify the second copy according to your needs. To help future yourself in the future, you can include `{{! musdtache comments }} to detail why and what your changes are about.
1. Create a patch file, taking care to preserve the original file path.
    ```shell
    mkdir -p src/patches/kotlin-client/libraries/jvm-okhttp/infrastructure
    diff -Naur build/tmp/openapi-templates/kotlin-client/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache \
               build/resources/patches/kotlin-client/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache \
               > src/patches/kotlin-client/libraries/jvm-okhttp/infrastructure/ApiClient.kt.mustache.patch
    ```

## Publication

1. Make sure you have the `gradle.write.user` and `gradle.write.key` variables set in
   your `~/.gradle/gradle.properties` file.  `user` is your github username and `key` is a github personal access token generated with `package:write` access.
2. Set the `version` in `build.gradle.kts`.  To create an non-snapshot version, remove the `-SNAPSHOT` then publish.  After that, set the version to the next SNAPSHOT version. 
3. Publish with `./gradlew :openapi-sdk:publish` on the parent folder (`project-root:gradle`)
