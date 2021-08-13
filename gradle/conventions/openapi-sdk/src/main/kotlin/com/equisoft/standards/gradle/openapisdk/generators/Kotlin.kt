package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun GenerateTask.configureKotlinSdkTask(openApiSdk: OpenApiSdkExtension) {
    generatorName.set("kotlin")

    packageName.set(groupId)
    apiPackage.set(packageName)
    invokerPackage.set(packageName.map { "$it.invoker" } )
    modelPackage.set(packageName.map { "$it.models" })

    configOptions.put("collectionType", "list")

    configureGradleSdkPrePostTasks()
}
