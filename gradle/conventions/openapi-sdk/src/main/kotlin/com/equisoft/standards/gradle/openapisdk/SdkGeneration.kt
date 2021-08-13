package com.equisoft.standards.gradle.openapisdk

import com.equisoft.standards.gradle.openapisdk.generators.configureKotlinSdkTask
import com.equisoft.standards.gradle.openapisdk.generators.configureMicronautSdkTask
import com.equisoft.standards.gradle.openapisdk.generators.configurePhpSdkTask
import com.equisoft.standards.gradle.openapisdk.generators.configureTypescriptSdkTask
import org.gradle.internal.logging.text.StyledTextOutput.Style.Header
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import kotlin.collections.Map.Entry

private val taskMappings: Map<String, ConfigureSdkTask> = mapOf(
    "kotlin" to GenerateTask::configureKotlinSdkTask,
    "micronaut" to GenerateTask::configureMicronautSdkTask,
    "php" to GenerateTask::configurePhpSdkTask,
    "typeScript" to GenerateTask::configureTypescriptSdkTask,
);

internal typealias ConfigureSdkTask = GenerateTask.(openApiSdk: OpenApiSdkExtension) -> Unit

internal fun TaskContainerScope.createSdkGenerationTasks(openApiSdk: OpenApiSdkExtension) {
    configureGenerateTaskDefaults(openApiSdk)
    taskMappings.forEach(registerGenerateTask(openApiSdk))
}

private fun TaskContainerScope.configureGenerateTaskDefaults(openApiSdk: OpenApiSdkExtension) = withType<GenerateTask> {
    inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
    outputDir.set(openApiSdk.outputDir.dir(generatorName).map { it.asFile.path })

    id.set("${project.rootProject.name}-sdk")
    groupId.set("${project.group}.sdk")
    version.set("${project.version}")

    templateDir.set(project.provider {
        val file = openApiSdk.customResourcesDir.dir(generatorName).map { it.asFile }.orNull
        if (file?.exists() == true) {
            file.path
        } else {
            null
        }
    })
}

private fun TaskContainerScope.registerGenerateTask(
    openApiSdk: OpenApiSdkExtension
): (Entry<String, ConfigureSdkTask>) -> Unit = { (name, configure) ->
    val displayName = name.capitalize()

    register<GenerateTask>("generate${displayName}Sdk") {
        group = OpenApiGeneratorPlugin.pluginGroup
        val output = createOutput()

        doLast {
            output.style(Header).println("Building ${generatorName.get()} SDK")
        }

        configure(openApiSdk)

        doLast {
            output.style(Success).println("$displayName SDK generated to: ${outputDir.get()}")
        }
    }
}

