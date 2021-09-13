package com.equisoft.standards.gradle.openapisdk.generators

import com.equisoft.standards.gradle.openapisdk.OpenApiSdkExtension
import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.ASSEMBLE
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.BUILD
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.CHECK
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.PUBLISH
import com.equisoft.standards.gradle.openapisdk.generators.SdkTask.SYNC
import com.equisoft.standards.gradle.openapisdk.tasks.CheckSdkTask
import com.equisoft.standards.gradle.openapisdk.tasks.PublishSdkTask
import com.equisoft.standards.gradle.openapisdk.tasks.SyncRepositoryTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.internal.logging.text.StyledTextOutput.Style.Success
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.OpenApiGeneratorPlugin
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

abstract class SdkGenerator(
    val displayName: String,
    val generatorName: String,
    protected val openApiSdk: OpenApiSdkExtension
) {
    private val taskGroup = "${OpenApiGeneratorPlugin.pluginGroup}/$displayName SDK"
    private val outputDirectory = openApiSdk.outputDir.dir(displayName.toLowerCase())

    abstract fun assembleSdk(task: GenerateTask)

    abstract fun checkSdk(task: CheckSdkTask)

    fun registerTasks(tasks: TaskContainer): Map<SdkTask, TaskProvider<*>> = with(tasks) {
        val sync = registerSync()
        val assemble = registerAssemble(sync.second)
        val check = registerCheck(assemble.second)
        val build = registerBuild(assemble.second, check.second)
        val publish = registerPublish(assemble.second, check.second, sync.second)

        return mapOf(sync, assemble, check, build, publish)
    }

    private fun TaskContainer.registerSync() = registerSdkTask<SyncRepositoryTask>(SYNC) {
        group = taskGroup
        onlyIf { openApiSdk.git.enable.get() }

        target.set(outputDirectory)
        host.set(openApiSdk.git.host)
        userId.set(openApiSdk.git.userId)
        repoId.set(openApiSdk.projectKey.map { "$it-sdk-${displayName.toLowerCase()}" })
        token.set(openApiSdk.git.token)
    }

    /**
     * Configure global options and settings on the OpenAPI generate task.
     *
     * See https://openapi-generator.tech/docs/globals/ for available options.
     */
    private fun TaskContainer.registerAssemble(
        sync: TaskProvider<SyncRepositoryTask>
    ) = registerSdkTask<GenerateTask>(ASSEMBLE) {
        group = taskGroup
        dependsOn(sync)

        id.set(openApiSdk.projectKey.map { "$it-sdk" })
        groupId.set(project.provider { "${project.group}.sdk" })
        version.set(project.provider { "${project.version}" })

        assembleSdk(this)

        generatorName.set(this@SdkGenerator.generatorName)
        inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
        outputDir.set(outputDirectory.map { it.asFile.path })

        gitHost.set(openApiSdk.withGitEnabled { sync.get().host.get() })
        gitUserId.set(openApiSdk.withGitEnabled { sync.get().userId.get() })
        gitRepoId.set(openApiSdk.withGitEnabled { sync.get().repoId.get() })

        doLast {
            createOutput().style(Success).println("$displayName SDK generated to: ${outputDir.get()}")
        }
    }

    private fun TaskContainer.registerCheck(
        assemble: TaskProvider<GenerateTask>
    ) = registerSdkTask<CheckSdkTask>(CHECK) {
        group = taskGroup
        dependsOn(assemble)

        checkSdk(this)

        directory.set(outputDirectory)
    }

    private fun TaskContainer.registerBuild(
        assemble: TaskProvider<GenerateTask>,
        check: TaskProvider<CheckSdkTask>
    ) = registerSdkTask<Task>(BUILD) {
        group = taskGroup
        dependsOn(assemble, check)
    }

    private fun TaskContainer.registerPublish(
        assemble: TaskProvider<GenerateTask>,
        check: TaskProvider<CheckSdkTask>,
        sync: TaskProvider<SyncRepositoryTask>
    ) = registerSdkTask<PublishSdkTask>(PUBLISH) {
        group = taskGroup
        dependsOn(assemble)
        mustRunAfter(check)
        onlyIf { openApiSdk.git.enable.get() }

        directory.set(outputDirectory)
        host.set(sync.get().host)
        userId.set(sync.get().userId)
        repoId.set(sync.get().repoId)
        token.set(sync.get().token)

        if (project.extra.has("is-release") && project.extra.get("is-release") == "true") {
            tag.set("v${project.version}")
        }
    }

    private inline fun <reified T : Task> TaskContainer.registerSdkTask(
        type: SdkTask,
        noinline configuration: T.() -> Unit
    ): Pair<SdkTask, TaskProvider<T>> =
        type to register(type.toTaskName(displayName), configuration)
}

enum class SdkTask(
    private val prefix: String,
) {
    SYNC("sync"),
    ASSEMBLE("assemble"),
    CHECK("check"),
    BUILD("build"),
    PUBLISH("publish");

    fun toTaskName(): String = toTaskName("")
    fun toTaskName(name: String): String = "${prefix}${name.toLowerCase().capitalize()}Sdk"
}
