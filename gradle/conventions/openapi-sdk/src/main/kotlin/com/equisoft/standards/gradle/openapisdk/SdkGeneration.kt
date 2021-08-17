package com.equisoft.standards.gradle.openapisdk

import com.equisoft.standards.gradle.openapisdk.generators.KotlinSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.MicronautSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.PhpSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.TypescriptSdkGenerator
import com.equisoft.standards.gradle.openapisdk.tasks.SdkTasks
import com.equisoft.standards.gradle.openapisdk.tasks.registerSdkTasks
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.withType
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun TaskContainerScope.createSdkGenerationTasks(openApiSdk: OpenApiSdkExtension) {
    configureGenerateTaskDefaults(openApiSdk)

    listOf(
        KotlinSdkGenerator(),
        MicronautSdkGenerator(),
        PhpSdkGenerator(),
        TypescriptSdkGenerator(),
    ).map {
        registerSdkTasks(openApiSdk, it)
    }.let(::configureSdkTasksAggregates)
}

private fun TaskContainerScope.configureGenerateTaskDefaults(openApiSdk: OpenApiSdkExtension) = withType<GenerateTask> {
    inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
    outputDir.set(openApiSdk.generatorOutputDir(generatorName))

    id.set(openApiSdk.projectKey.map { "$it-sdk" })
    groupId.set("${project.group}.sdk")
    version.set("${project.version}")

    val git = openApiSdk.git
    gitHost.set(git.ifEnabled { host.get() })
    gitUserId.set(git.ifEnabled { userId.get() })
    gitRepoId.set(git.ifEnabled { "${id.get()}-${generatorName.get()}" })

    templateDir.set(project.provider {
        val file = openApiSdk.customResourcesDir.dir(generatorName).map { it.asFile }.orNull
        if (file?.exists() == true) {
            file.path
        } else {
            null
        }
    })
}

private fun TaskContainerScope.configureSdkTasksAggregates(tasks: List<SdkTasks>) {
    mapOf<String, SdkTasks.() -> TaskProvider<*>>(
        "sync" to { sync },
        "assemble" to { assemble },
        "check" to { check },
        "build" to { build },
        "publish" to { publish },
    ).forEach { (action, transform) ->
        register("${action}Sdk") {
            group = "${OpenApiGeneratorPlugin.pluginGroup}/sdk"
            dependsOn(tasks.map(transform))
        }
    }
}
