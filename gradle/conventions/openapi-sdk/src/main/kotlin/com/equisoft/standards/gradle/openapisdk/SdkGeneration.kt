package com.equisoft.standards.gradle.openapisdk

import com.equisoft.standards.gradle.openapisdk.generators.KotlinSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.MicronautSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.PhpSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.TypescriptSdkGenerator
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import kotlin.collections.Map.Entry

internal fun TaskContainerScope.createSdkGenerationTasks(openApiSdk: OpenApiSdkExtension) {
    configureGenerateTaskDefaults(openApiSdk)

    mapOf(
        "kotlin" to KotlinSdkGenerator(),
        "micronaut" to MicronautSdkGenerator(),
        "php" to PhpSdkGenerator(),
        "typescript" to TypescriptSdkGenerator(),
    ).forEach(registerSdkTasks(openApiSdk))
}

private fun TaskContainerScope.configureGenerateTaskDefaults(openApiSdk: OpenApiSdkExtension) = withType<GenerateTask> {
    inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
    outputDir.set(openApiSdk.generatorOutputDir(generatorName))

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

private fun TaskContainerScope.registerSdkTasks(
    openApiSdk: OpenApiSdkExtension
): (Entry<String, SdkGenerator>) -> Unit = { (name, generator) ->
    val displayName = name.capitalize()

    register<GenerateTask>("assemble${displayName}Sdk") {
        group = OpenApiSdkExtension.PLUGIN_GROUP

        generator.configureGenerateTask(this, openApiSdk)

        doLast {
            createOutput().style(Success).println("$displayName SDK generated to: ${outputDir.get()}")
        }
    }

    register("check${displayName}Sdk") {
        group = OpenApiSdkExtension.PLUGIN_GROUP
        mustRunAfter("assemble${displayName}Sdk")

        generator.configureChecks(this, openApiSdk)
    }

    register("build${displayName}Sdk") {
        group = OpenApiSdkExtension.PLUGIN_GROUP
        dependsOn("assemble${displayName}Sdk", "check${displayName}Sdk")
    }
}
