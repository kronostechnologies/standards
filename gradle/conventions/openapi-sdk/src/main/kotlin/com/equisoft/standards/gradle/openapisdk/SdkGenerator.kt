package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Task
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

interface SdkGenerator {
    val displayName: String
    val generatorName: String

    fun configureGenerateTask(task: GenerateTask, openApiSdk: OpenApiSdkExtension)
    fun configureChecks(task: Task, openApiSdk: OpenApiSdkExtension)

    fun defaultGroup(): String = "${OpenApiGeneratorPlugin.pluginGroup}/$displayName SDK"
}
