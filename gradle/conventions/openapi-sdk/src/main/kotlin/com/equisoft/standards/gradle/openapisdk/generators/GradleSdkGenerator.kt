package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

abstract class GradleSdkGenerator(
    private val generatorName: String
) : SdkGenerator {
    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        generatorName.set(this@GradleSdkGenerator.generatorName)

        packageName.set(groupId)
        apiPackage.set(packageName)
        invokerPackage.set(packageName.map { "$it.invoker" })
        modelPackage.set(packageName.map { "$it.models" })

        doFirst {
            val path = outputDir.get()
            project.delete("$path/src", "$path/docs", "$path/build")
        }

        doLast {
            project.exec {
                workingDir(outputDir.get())
                commandLine("chmod", "+x", "gradlew")
            }
        }
    }

    override fun configureChecks(
        task: Task,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        doLast {
            project.exec {
                workingDir(openApiSdk.generatorOutputDir(generatorName).get())
                commandLine("./gradlew", "build")
            }
        }
    }
}
