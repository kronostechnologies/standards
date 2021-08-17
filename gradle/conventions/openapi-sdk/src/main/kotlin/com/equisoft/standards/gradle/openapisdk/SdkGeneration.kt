package com.equisoft.standards.gradle.openapisdk

import com.equisoft.standards.gradle.openapisdk.generators.KotlinSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.MicronautSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.PhpSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask
import com.equisoft.standards.gradle.openapisdk.generators.TypescriptSdkGenerator
import org.gradle.api.tasks.TaskContainer
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin

internal fun TaskContainer.registerSdkTasks(openApiSdk: OpenApiSdkExtension) {
    listOf(
        KotlinSdkGenerator(openApiSdk),
        MicronautSdkGenerator(openApiSdk),
        PhpSdkGenerator(openApiSdk),
        TypescriptSdkGenerator(openApiSdk),
    ).map {
        it.registerTasks(this)
    }.let { tasks ->
        SdkTask.values().forEach { type ->
            register(type.toTaskName()) {
                group = "${OpenApiGeneratorPlugin.pluginGroup}/sdk"
                dependsOn(tasks.map { it[type] })
            }
        }
    }
}
