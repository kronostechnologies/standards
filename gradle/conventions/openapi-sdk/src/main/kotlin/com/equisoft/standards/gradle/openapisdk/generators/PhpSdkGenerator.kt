package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class PhpSdkGenerator : SdkGenerator {
    override val displayName: String
        get() = "PHP"
    override val generatorName: String
        get() = "php"

    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        generatorName.set(this@PhpSdkGenerator.generatorName)

        val camelCaseName = openApiSdk.projectKey.map { it.kebabToUpperCamelCase() }
        packageName.set(camelCaseName.map { "$it SDK" })
        invokerPackage.set(camelCaseName.map { "Equisoft\\SDK\\$it" })

        configOptions.put("variableNamingConvention", "camelCase")

        doFirst {
            val path = outputDir.get()
            project.delete("$path/libs", "$path/docs", "$path/test", "$path/vendor")
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
                commandLine("composer", "install")
            }
            project.exec {
                workingDir(path)
                commandLine("./vendor/bin/phpunit")
            }
        }
    }
}
