package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.exec
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import org.openapitools.codegen.CodegenConstants.ENUM_PROPERTY_NAMING
import org.openapitools.codegen.CodegenConstants.ENUM_UNKNOWN_DEFAULT_CASE
import org.openapitools.codegen.CodegenConstants.SUPPORTS_ES6
import org.openapitools.codegen.languages.AbstractTypeScriptClientCodegen.NPM_NAME
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class TypescriptSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : SdkGenerator(
    displayName = "Typescript",
    generatorName = "typescript-fetch",
    openApiSdk
) {
    @Suppress("LongMethod")
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        packageName.set(openApiSdk.projectKey.map { "${it.kebabToUpperCamelCase()} SDK" })
        invokerPackage.set(id)

        configOptions.set(
            project.provider {
                mapOf(
                    ENUM_PROPERTY_NAMING to "original",
                    NPM_NAME to id.get(), // npmName is required for the project's structure to be generated (ie src/)
                    SUPPORTS_ES6 to "true",
                    "typescriptThreePlus" to "true",
                    "variableNamingConvention" to "camelCase",
                    ENUM_UNKNOWN_DEFAULT_CASE to "true",
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
