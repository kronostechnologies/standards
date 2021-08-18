package com.equisoft.standards.gradle.openapisdk

import com.equisoft.standards.gradle.openapisdk.generators.KotlinSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.MicronautSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.PhpSdkGenerator
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.ASSEMBLE
import com.equisoft.standards.gradle.openapisdk.generators.TypescriptSdkGenerator
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin

internal fun TaskContainer.registerSdkTasks(openApiSdk: OpenApiSdkExtension) {
    listOf(
        KotlinSdkGenerator(openApiSdk),
        MicronautSdkGenerator(openApiSdk),
        PhpSdkGenerator(openApiSdk),
        TypescriptSdkGenerator(openApiSdk),
    ).map {
        it.registerTasks(this)
    }.let { tasks ->
        tasks.mapNotNull { it[ASSEMBLE] }.forEach {
            // This has to be done inside every task because they each have their own classloader
            it.get().doFirst { validateClasspath(this) }
        }

        SdkTask.values().forEach { type ->
            register(type.toTaskName()) {
                group = "${OpenApiGeneratorPlugin.pluginGroup}/sdk"
                dependsOn(tasks.map { it[type] })
            }
        }
    }
}

private fun validateClasspath(task: Task) {
    val sampleFile = "/kotlin-client/build.gradle.mustache"
    val sampleResource = task.javaClass.getResource(sampleFile)
    val regex = Regex("""file:(/[^/]+)+/openapi-sdk-[^/]+\.jar!${sampleFile.replace(".", "\\.")}""")

    if (sampleResource?.path?.matches(regex) != true) {
        throw GradleException(
            "OpenApi SDK does not appear high enough in your classpath. " +
                "This will cause monkey-patches to not be loaded. " +
                "Make sure that OpenAPI generator is not explicitly specified or that it appears AFTER OpenAPI SDK."
        )
    }
}
