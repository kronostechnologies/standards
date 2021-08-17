package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class KotlinSdkGenerator : GradleSdkGenerator() {
    override val displayName: String
        get() = "Kotlin"
    override val generatorName: String
        get() = "kotlin"

    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        super.configureGenerateTask(task, openApiSdk)

        configOptions.put("collectionType", "list")
    }
}
