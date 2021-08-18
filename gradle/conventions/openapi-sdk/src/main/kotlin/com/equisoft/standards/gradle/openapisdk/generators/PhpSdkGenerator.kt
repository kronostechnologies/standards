package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.exec
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class PhpSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : SdkGenerator(
    displayName = "PHP",
    generatorName = "php",
    openApiSdk
) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        packageName.set(openApiSdk.projectKey.map { "${it.kebabToUpperCamelCase()} SDK" })
        invokerPackage.set(groupId.map { groupId ->
            groupId
                .split(".")
                .dropWhile { it.length <= 4 } // Attempt to drop usual TLDs
                .joinToString("\\")
        })

        configOptions.put("variableNamingConvention", "camelCase")

        doFirst {
            val path = outputDir.get()
            project.delete("$path/libs", "$path/docs", "$path/test", "$path/vendor")
        }
    }

    override fun checkSdk(task: CheckSdkTask): Unit = with(task) {
        doLast {
            project.exec(directory, "composer", "install")
            project.exec(directory, "./vendor/bin/phpunit")
        }
    }
}
