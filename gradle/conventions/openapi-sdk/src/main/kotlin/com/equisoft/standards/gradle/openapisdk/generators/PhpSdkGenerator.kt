package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class PhpSdkGenerator : SdkGenerator {
    private val generatorName = "php"

    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        generatorName.set(this@PhpSdkGenerator.generatorName)

        val camelCaseName = project.rootProject.name.kebabToUpperCamelCase()
        packageName.set("$camelCaseName SDK")
        invokerPackage.set("Equisoft\\SDK\\$camelCaseName")

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
