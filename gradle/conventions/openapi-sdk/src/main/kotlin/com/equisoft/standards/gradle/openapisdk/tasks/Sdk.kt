package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.SdkGenerator
import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.exec
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.text.StyledTextOutput.Style.Info
import org.gradle.internal.logging.text.StyledTextOutput.Style.Normal
import org.gradle.kotlin.dsl.TaskContainerScope
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal data class SdkTasks(
    val sync: TaskProvider<*>,
    val assemble: TaskProvider<*>,
    val check: TaskProvider<*>,
    val build: TaskProvider<*>,
    val publish: TaskProvider<*>,
)

internal fun TaskContainerScope.registerSdkTasks(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator
): SdkTasks {
    val sync = registerSyncTask(openApiSdk, generator)
    val assemble = registerAssembleTask(openApiSdk, generator, sync)
    val check = registerCheckTask(openApiSdk, generator, assemble)
    val build = registerBuildTask(openApiSdk, generator, sync, assemble, check)
    val publish = registerPublishTask(openApiSdk, generator, sync, build)

    return SdkTasks(sync, assemble, check, build, publish)
}

private fun TaskContainerScope.registerSyncTask(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator
) = register<SyncRepositoryTask>("sync${generator.displayName}Sdk") {
    group = generator.defaultGroup()

    target.set(project.file(openApiSdk.generatorOutputDir(generator.generatorName)))
    uri.set(openApiSdk.git.ifEnabled {
        "git@${host.get()}:${userId.get()}/${openApiSdk.projectKey.get()}-sdk-${generator.displayName.toLowerCase()}.git"
    })
}

private fun TaskContainerScope.registerCheckTask(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator,
    assemble: TaskProvider<GenerateTask>
) = register("check${generator.displayName}Sdk") {
    group = generator.defaultGroup()
    dependsOn(assemble)

    generator.configureChecks(this, openApiSdk)
}

private fun TaskContainerScope.registerBuildTask(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator,
    sync: TaskProvider<SyncRepositoryTask>,
    assemble: TaskProvider<GenerateTask>,
    check: TaskProvider<Task>
) = register("build${generator.displayName}Sdk") {
    group = generator.defaultGroup()
    dependsOn(project.provider {
        val tasks = mutableListOf(assemble, check)
        if (openApiSdk.git.enable.get()) {
            tasks.add(sync)
        }

        tasks
    })
}

private fun TaskContainerScope.registerPublishTask(
    openApiSdk: OpenApiSdkExtension,
    generator: SdkGenerator,
    sync: TaskProvider<SyncRepositoryTask>,
    build: TaskProvider<Task>
) = register("publish${generator.displayName}Sdk") {
    group = generator.defaultGroup()
    dependsOn(sync, build)

    doLast {
        val lastCommit = project.exec("git", "rev-parse", "--short", "HEAD")
        val repoHandle = project.exec("git", "remote", "get-url", "origin")
            .replace(Regex(""".+@.+:([\w\d.-]+/[\w\d.-]+)\.git"""), "$1")
        val commitMessage = "chore: update generated code from $repoHandle@$lastCommit"
        val outputDir = openApiSdk.generatorOutputDir(generator.generatorName).get()

        createOutput()
            .style(Normal).println("You can publish the SDK files by running:")
            .style(Info)
            .println("  cd \"$outputDir\"")
            .println("    && git add -A")
            .println("    && git commit -m \"$commitMessage\"")
            .println("    && git push origin HEAD")
    }
}
