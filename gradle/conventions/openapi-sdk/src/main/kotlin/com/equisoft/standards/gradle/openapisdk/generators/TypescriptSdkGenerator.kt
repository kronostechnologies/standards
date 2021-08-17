package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class TypescriptSdkGenerator : SdkGenerator {
    override val displayName: String
        get() = "Typescript"
    override val generatorName: String
        get() = "typescript-fetch"

    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        generatorName.set(this@TypescriptSdkGenerator.generatorName)

        packageName.set(openApiSdk.projectKey.map { "${it.kebabToUpperCamelCase()} SDK" })
        invokerPackage.set(id)

        configOptions.set(project.provider {
            mapOf(
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
    }

    override fun configureChecks(
        task: Task,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        doLast {
            val path = openApiSdk.generatorOutputDir(generatorName).get()

            project.exec {
                workingDir(path)
                commandLine("yarn", "install", "--immutable")
            }
            project.exec {
                workingDir(path)
                commandLine("yarn", "build")
            }
        }
    }
}
