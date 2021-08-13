package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun GenerateTask.configureTypescriptSdkTask(openApiSdk: OpenApiSdkExtension) {
    generatorName.set("typescript-fetch")

    packageName.set("${project.rootProject.name.kebabToUpperCamelCase()} SDK")
    invokerPackage.set("${project.rootProject.name}-sdk")

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

    doLast {
        val path = outputDir.get()

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
