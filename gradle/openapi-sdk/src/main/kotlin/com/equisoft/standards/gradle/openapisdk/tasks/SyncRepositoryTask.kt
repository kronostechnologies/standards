package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.exec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput.Style.Error

abstract class SyncRepositoryTask : GitTask() {
    @get:Optional
    @get:Input
    abstract val overwrite: Property<Boolean?>

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val defaultBranch: Property<String?>

    init {
        outputs.upToDateWhen {
            if (isSdkCloned()) {
                sdkGit("fetch", "origin", displayResult = true)
                sdkGit("rev-parse", "HEAD") == sdkGit("rev-parse", "origin/HEAD")
            } else {
                false
            }
        }
    }

    @TaskAction
    fun cloneRepository() {
        val isValid = runCatching {
            val valid = isSdkCloned()
            if (valid) {
                cleanupRepository()
            }

            valid
        }.onFailure {
            createOutput()
                .style(Error).text("Failed to sync repository in '$target'.")
                .exception(it)
        }.getOrDefault(false)

        if (!isValid) {
            recreateRepository()
        }
    }

    private fun isSdkCloned() =
        target.get().asFile.path == sdkGit("rev-parse", "--show-toplevel")
            && uri.get() == sdkGit("remote", "get-url", "origin")

    private fun cleanupRepository() {
        val defaultBranch = defaultBranch()

        sdkGit("stash", "save", "--keep-index", "--include-untracked")
        sdkGit("checkout", defaultBranch)
        sdkGit("reset", "--hard", "origin/$defaultBranch")
    }

    private fun defaultBranch(): String = this.defaultBranch.orElse(
        target.asFile.map { directory ->
            project.exec(directory, "git", "remote", "show", "origin").lines()
                .first { it.trim().startsWith("HEAD branch: ") }
                .replace("HEAD branch: ", "")
                .trim()
        }
    ).get()

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

    private fun sdkGit(vararg arguments: String, displayResult: Boolean = false): String =
        project.exec(target.get().asFile, "git", *arguments, displayResult = displayResult)
}
