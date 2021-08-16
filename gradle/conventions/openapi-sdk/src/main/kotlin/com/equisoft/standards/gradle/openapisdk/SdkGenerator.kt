package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

interface SdkGenerator {
    fun configureGenerateTask(task: GenerateTask, openApiSdk: OpenApiSdkExtension)
    fun configureChecks(task: Task, openApiSdk: OpenApiSdkExtension)
}
