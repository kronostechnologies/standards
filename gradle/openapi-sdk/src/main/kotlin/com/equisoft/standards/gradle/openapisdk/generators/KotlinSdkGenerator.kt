package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class KotlinSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : GradleSdkGenerator(
    displayName = "Kotlin",
    generatorName = "kotlin",
    openApiSdk
) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        super.assembleSdk(this)

        configOptions.put("collectionType", "list")
    }
}
