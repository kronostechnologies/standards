package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

class OpenApiSdkPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply(OpenApiGeneratorPlugin::class.java)

        val openApiSdk = extensions.create<OpenApiSdkExtension>("openApiSdk", project)

        afterEvaluate {
            configureOpenApiSpecGeneration(openApiSdk)
        }

        tasks {
            named("check") {
                dependsOn("openApiValidate")
            }

            withType<ValidateTask> {
                inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
            }

            registerSdkTasks(openApiSdk)
        }
    }
}

