package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.exec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput.Style.Error

abstract class SyncRepositoryTask : DefaultTask() {
    @get:Input
    abstract val host: Property<String>
    @get:Input
    abstract val userId: Property<String>
    @get:Input
    abstract val repoId: Property<String>

    @get:Internal
    val uri: Provider<String> = host.map { "git@$it:${userId.get()}/${repoId.get()}.git" }

    @get:Optional
    @get:Input
    abstract val overwrite: Property<Boolean?>

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    @TaskAction
    fun cloneRepository() {
        val isValid = runCatching {
            val isRemoteValid = validateRepository()
            if (isRemoteValid) {
                cleanupRepository()
            }

            isRemoteValid
        }.onFailure {
            createOutput()
                .style(Error).text("Failed to sync repository in '$target'.")
                .exception(it)
        }.getOrDefault(false)

        if (!isValid) {
            recreateRepository()
        }
    }

    private fun validateRepository(): Boolean {
        val directory = target.get().asFile
        val repositoryRoot = project.exec(directory, "git", "rev-parse", "--show-toplevel")
        val repositoryRemoteUri = project.exec(directory, "git", "remote", "get-url", "origin")

        return repositoryRoot == directory.path
            && repositoryRemoteUri == uri.get()
    }

    private fun cleanupRepository() {
        val directory = target.get().asFile
        val defaultBranch = project.exec(directory, "git", "symbolic-ref", "refs/remotes/origin/HEAD").split("/").last()

        project.exec(directory, "git", "stash", "save", "--keep-index", "--include-untracked")
        project.exec(directory, "git", "checkout", defaultBranch)
        project.exec(directory, "git", "fetch")
        project.exec(directory, "git", "reset", "--hard", "origin/$defaultBranch")
    }

    private fun recreateRepository() {
        val directory = target.get().asFile

        if (overwrite.getOrElse(true)) {
            project.delete(directory)
            directory.mkdirs()
            project.exec("git", "clone", uri.get(), directory.path)
        } else {
            createOutput()
                .style(Error).text("Repository at ${directory.path} is invalid but overwrite is set to 'false'.")
        }
    }
}
