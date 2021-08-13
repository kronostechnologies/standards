package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun GenerateTask.configureMicronautSdkTask(openApiSdk: OpenApiSdkExtension) {
    generatorName.set("micronaut")

    id.set("${project.rootProject.name}-sdk-micronaut")

    packageName.set(groupId)
    apiPackage.set(packageName)
    invokerPackage.set(packageName.map { "$it.invoker" } )
    modelPackage.set(packageName.map { "$it.models" })

    configOptions.set(
        mapOf(
            "clientId" to project.rootProject.name,
            "introspected" to "true",
            "jacksonDatabindNullable" to "false",
            "supportAsync" to "false",
            "useGenericResponse" to "true",
            "useOptional" to "false",
            "useReferencedSchemaAsDefault" to "false"
        )
    )

    configureGradleSdkPrePostTasks()
}
