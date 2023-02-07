package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.exec
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import org.openapitools.codegen.CodegenConstants.ENUM_UNKNOWN_DEFAULT_CASE
import org.openapitools.codegen.languages.PhpClientCodegen.VARIABLE_NAMING_CONVENTION
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

private const val TLD_LENGTH: Int = 4

class PhpSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : SdkGenerator(
    displayName = "PHP",
    generatorName = "php",
    openApiSdk
) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        packageName.set(openApiSdk.projectKey.map { "${it.kebabToUpperCamelCase()} SDK" })
        invokerPackage.set(groupId.map(::transformGroupIdToPhpNamespace))

        configOptions.putAll(
            project.provider {
                mapOf(
                    VARIABLE_NAMING_CONVENTION to "camelCase",
                    ENUM_UNKNOWN_DEFAULT_CASE to "true",
                )
            }
        )

        doFirst {
            val path = outputDir.get()
            project.delete("$path/libs", "$path/docs", "$path/test", "$path/vendor")
        }
    }

    private fun transformGroupIdToPhpNamespace(groupId: String) = groupId
        .split(".")
        .dropWhile { it.length <= TLD_LENGTH } // Attempt to drop usual TLDs
        .joinToString("\\")

    override fun checkSdk(task: CheckSdkTask): Unit = with(task) {
        doLast {
            project.exec(directory, "composer", "install", displayResult = true)
            project.exec(directory, "./vendor/bin/phpunit", displayResult = true)
        }
    }
}
