package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.exec
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

abstract class GradleSdkGenerator(
    displayName: String,
    generatorName: String,
    openApiSdk: OpenApiSdkExtension
) : SdkGenerator(displayName, generatorName, openApiSdk) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        packageName.set(groupId)
        apiPackage.set(packageName)
        invokerPackage.set(packageName.map { "$it.invoker" })
        modelPackage.set(packageName.map { "$it.models" })

        configOptions.put("enumPropertyNaming", "UPPERCASE")

        doFirst {
            val path = outputDir.get()
            project.delete("$path/src", "$path/docs", "$path/build")
        }

        doLast {
            project.exec(outputDir, "chmod", "+x", "gradlew")
        }
    }

    override fun checkSdk(task: CheckSdkTask): Unit = with(task) {
        doLast {
            project.exec(directory, "./gradlew", "build", displayResult = true)
        }
    }
}