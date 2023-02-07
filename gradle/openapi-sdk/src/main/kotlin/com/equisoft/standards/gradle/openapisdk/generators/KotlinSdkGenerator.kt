package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import org.openapitools.codegen.CodegenConstants.ENUM_UNKNOWN_DEFAULT_CASE
import org.openapitools.codegen.languages.KotlinClientCodegen.COLLECTION_TYPE
import org.openapitools.codegen.languages.KotlinClientCodegen.CollectionType
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

        configOptions.put(COLLECTION_TYPE, CollectionType.LIST.value)
    }
}
