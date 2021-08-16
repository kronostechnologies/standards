package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class MicronautSdkGenerator : GradleSdkGenerator("micronaut") {
    override fun configureGenerateTask(
        task: GenerateTask,
        openApiSdk: OpenApiSdkExtension
    ): Unit = with(task) {
        super.configureGenerateTask(task, openApiSdk)

        id.set("${project.rootProject.name}-sdk-micronaut")

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
    }
}
