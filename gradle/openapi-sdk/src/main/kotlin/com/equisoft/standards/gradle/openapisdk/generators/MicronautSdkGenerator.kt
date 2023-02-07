package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.codegen.CodegenConstants
import org.openapitools.codegen.CodegenConstants.ENUM_UNKNOWN_DEFAULT_CASE
import org.openapitools.codegen.languages.AbstractJavaCodegen.OPENAPI_NULLABLE
import org.openapitools.codegen.languages.AbstractJavaCodegen.SUPPORT_ASYNC
import org.openapitools.codegen.languages.MicronautCodegen.CLIENT_ID
import org.openapitools.codegen.languages.MicronautCodegen.INTROSPECTED
import org.openapitools.codegen.languages.features.OptionalFeatures.USE_OPTIONAL
import org.openapitools.codegen.languages.features.UseGenericResponseFeatures.USE_GENERIC_RESPONSE
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class MicronautSdkGenerator(
    openApiSdk: OpenApiSdkExtension
) : GradleSdkGenerator(
    displayName = "Micronaut",
    generatorName = "micronaut",
    openApiSdk
) {
    @Suppress("LongMethod")
    override fun assembleSdk(task: GenerateTask): Unit = with(task) {
        super.assembleSdk(this)

        val gitProject = openApiSdk.projectKey.map { "$it-sdk-micronaut" }
        id.set(gitProject)

        configOptions.putAll(
            project.provider {
                mapOf(
                    CLIENT_ID to openApiSdk.projectKey.get(),
                    INTROSPECTED to "true",
                    OPENAPI_NULLABLE to "false",
                    SUPPORT_ASYNC to "false",
                    USE_GENERIC_RESPONSE to "true",
                    USE_OPTIONAL to "false"
                )
            }
        )

        additionalProperties.putAll(
            project.provider {
                val gitProjectNameValue = gitProject.get()
                mapOf(
                    "gitProjectName" to gitProjectNameValue,
                    "packageUrl" to "https://maven.pkg.github.com/${openApiSdk.git.userId.get()}/$gitProjectNameValue"
                )
            }
        )
    }
}
