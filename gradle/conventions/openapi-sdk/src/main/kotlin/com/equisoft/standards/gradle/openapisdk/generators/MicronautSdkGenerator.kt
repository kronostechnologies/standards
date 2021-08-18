package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class MicronautSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : GradleSdkGenerator(
    displayName = "Micronaut",
    generatorName = "micronaut",
    openApiSdk
) {
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        super.assembleSdk(this)

        id.set(openApiSdk.projectKey.map { "$it-sdk-micronaut" })

        configOptions.set(project.provider {
            mapOf(
                "clientId" to openApiSdk.projectKey.get(),
                "introspected" to "true",
                "jacksonDatabindNullable" to "false",
                "supportAsync" to "false",
                "useGenericResponse" to "true",
                "useOptional" to "false",
                "useReferencedSchemaAsDefault" to "false"
            )
        })
    }
}
