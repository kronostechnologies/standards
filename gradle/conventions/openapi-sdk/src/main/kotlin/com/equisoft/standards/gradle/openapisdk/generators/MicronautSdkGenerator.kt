package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class MicronautSdkGenerator : GradleSdkGenerator() {
    override val displayName: String
        get() = "Micronaut"
    override val generatorName: String
        get() = "micronaut"

    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        super.configureGenerateTask(task, openApiSdk)

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
