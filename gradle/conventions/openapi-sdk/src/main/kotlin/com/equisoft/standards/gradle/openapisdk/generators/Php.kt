package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.kebabToUpperCamelCase
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun GenerateTask.configurePhpSdkTask(openApiSdk: OpenApiSdkExtension) {
    generatorName.set("php")

    val camelCaseName = project.rootProject.name.kebabToUpperCamelCase()
    packageName.set("$camelCaseName SDK")
    invokerPackage.set("Equisoft\\SDK\\$camelCaseName")

    configOptions.put("variableNamingConvention", "camelCase")

    doFirst {
        val path = outputDir.get()
        project.delete("$path/libs", "$path/docs", "$path/test", "$path/vendor")
    }

    doLast {
        val path = outputDir.get()

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
