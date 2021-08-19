package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.exec
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class TypescriptSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : SdkGenerator(
    displayName = "Typescript",
    generatorName = "typescript-fetch",
    openApiSdk
) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        packageName.set(openApiSdk.projectKey.map { "${it.kebabToUpperCamelCase()} SDK" })
        invokerPackage.set(id)

        configOptions.set(
            project.provider {
                mapOf(
                    "enumPropertyNaming" to "original",
                    "npmName" to id.get(), // npmName is required for the project's structure to be generated (ie src/)
                    "supportsES6" to "true",
                    "typescriptThreePlus" to "true",
                    "variableNamingConvention" to "camelCase",
                )
            }
        )

        doFirst {
            val path = outputDir.get()
            project.delete("$path/src", "$path/docs", "$path/dist")
        }
    }

    override fun checkSdk(task: CheckSdkTask): Unit = with(task) {
        doLast {
            project.exec(directory, "yarn", "install", "--immutable", displayResult = true)
            project.exec(directory, "yarn", "build", displayResult = true)
        }
    }
}
