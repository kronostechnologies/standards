package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.WriteProperties
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

internal fun Project.configureOpenApiSpecGeneration(openApiSdk: OpenApiSdkExtension) {
    // We don't have to guess versions later on if Micronaut's BOM is resolved
    val isMicronautResolved = configurations.any { configuration ->
        configuration.dependencies.any {
            it.group == "io.micronaut" && it.name == "micronaut-bom"
        }
    }

    if (isMicronautResolved) {
        configureMicronautOpenApi(project, openApiSdk)
        pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
            configureKapt(openApiSdk)
        }
    }
}

private fun configureMicronautOpenApi(project: Project, openApiSdk: OpenApiSdkExtension) = with(project) {
    dependencies {
        // micronaut-bom defines swagger-annotations but we allow overriding the version
        val swaggerVersionQualifier = openApiSdk.swaggerVersion.map { ":$it" }.orElse("").get()
        "implementation"("io.swagger.core.v3:swagger-annotations$swaggerVersionQualifier")
    }

    tasks {
        register<WriteProperties>("generateOpenapiProperties") {
            group = OpenApiGeneratorPlugin.pluginGroup
            destinationFile.set(openApiSdk.openApiPropertiesFile.get().asFile)

            property("micronaut.openapi.expand.api.version", version)
            property("mapping.path", "docs/openapi")
            property("rapidoc.enabled", "true")
            property("redoc.enabled", "true")
            property("swagger-ui.enabled", "true")
        }
    }
}

private fun Project.configureKapt(openApiSdk: OpenApiSdkExtension) {
    dependencies {
        "kapt"("io.micronaut.openapi:micronaut-openapi")
    }

    extensions.configure(KaptExtension::class.java) {
        arguments {
            arg("micronaut.openapi.config.file", openApiSdk.openApiPropertiesFile.asFile.get().path)
        }
    }

    tasks {
        addOpenApiPropertiesToKaptKotlin(openApiSdk)

        withType<ValidateTask> {
            dependsOn("kaptKotlin")
        }

        withType<GenerateTask> {
            dependsOn("kaptKotlin")
        }
    }
}

private fun TaskContainerScope.addOpenApiPropertiesToKaptKotlin(openApiSdk: OpenApiSdkExtension) {
    matching {
        it.name == "kaptKotlin"
    }.all {
        dependsOn("generateOpenapiProperties")

        inputs.file(openApiSdk.openApiPropertiesFile)
            .withPropertyName("openApiProperties")
            .withPathSensitivity(RELATIVE)
    }
}
