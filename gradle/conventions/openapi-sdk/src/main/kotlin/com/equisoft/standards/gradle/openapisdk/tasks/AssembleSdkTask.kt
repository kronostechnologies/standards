package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import com.equisoft.standards.gradle.openapisdk.createOutput
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun TaskContainerScope.registerAssembleTask(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator,
    sync: TaskProvider<SyncRepositoryTask>
) = register<GenerateTask>("assemble${generator.displayName}Sdk") {
    group = generator.defaultGroup()
    mustRunAfter(sync)

    generator.configureGenerateTask(this, openApiSdk)

    doLast {
        createOutput().style(Success).println("${generator.displayName} SDK generated to: ${outputDir.get()}")
    }
}
