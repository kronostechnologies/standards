package com.equisoft.standards.gradle.openapisdk

import org.gradle.internal.logging.text.StyledTextOutput.Style.Header
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.kotlin.dsl.withType
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun TaskContainerScope.createSdkGenerationTasks(openApiSdk: OpenApiSdkExtension) {
    withType<GenerateTask> {
        applyOpenApiSdkExtension(openApiSdk)
    }

    register<GenerateTask>("generateTypeScriptSdk") {
        configureTypescriptTask()
    }
}

private fun GenerateTask.applyOpenApiSdkExtension(openApiSdk: OpenApiSdkExtension) {
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

private fun GenerateTask.configureTypescriptTask() {
    generatorName.set("typescript-fetch")
    configOptions.set(project.provider {
        mapOf(
            "authorName" to "Equisoft Inc.",
            "enumPropertyNaming" to "original",
            "npmName" to "@equisoft/${id.get()}", // npmName is required for the project's structure to be generated (src/, ...)
            "supportsES6" to "true",
            "typescriptThreePlus" to "true",
            "variableNamingConvention" to "camelCase",
        )
    })

    doFirst {
        val path = outputDir.get()
        project.delete("$path/src", "$path/docs", "$path/dist")
    }

    doLast {
        val path = outputDir.get()
        val output = project.serviceOf<StyledTextOutputFactory>().create("an-output")

        output
            .style(Header)
            .println("Building SDK")

        project.exec {
            workingDir(path)
            commandLine("yarn", "install", "--immutable")
        }
        project.exec {
            workingDir(path)
            commandLine("yarn", "build")
        }

        output
            .style(Success)
            .println("Typescript SDK generated to: $path")
    }
}
