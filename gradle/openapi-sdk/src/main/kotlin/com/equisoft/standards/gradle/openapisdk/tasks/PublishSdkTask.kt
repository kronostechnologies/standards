package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.exec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput.Style.Normal

abstract class PublishSdkTask : GitTask() {
    @get:InputDirectory
    abstract val directory: DirectoryProperty

    @get:Optional
    @get:Input
    abstract val tag: Property<String>

    @TaskAction
    fun publish() {
        val directory = directory.get().asFile
        val output = createOutput()

        val hasSomethingToCommit = project.exec(directory, "git", "status", "--porcelain").isNotBlank()
        if (hasSomethingToCommit) {
            output.style(Normal).println("Committing changes")
            project.exec(directory, "git", "add", "-A", displayResult = true)
            project.exec(directory, "git", "commit", "-m", getCommitMessage(), displayResult = true)
            project.exec(directory, "git", "push", "origin", "HEAD", displayResult = true)
        } else {
            output.style(Normal).println("Nothing to commit")
        }

        if (tag.isPresent) {
            output.style(Normal).println("Tag remote repository")
            val tagMessage = "Release ${tag.get()}"
            project.exec(directory, "git", "tag", "-a", tag.get(), "-m", tagMessage, displayResult = true)
            project.exec(directory, "git", "push", "origin", "--tags")
        }
    }

    private fun getCommitMessage(): String {
        val repoHandle = project.exec("git", "remote", "get-url", "origin")
            .replace(Regex(""".+@.+:([\w\d.-]+/[\w\d.-]+)\.git"""), "$1")
        val lastCommit = project.exec("git", "rev-parse", "--short", "HEAD")
        return "chore: generated from $repoHandle/commit/$lastCommit"
    }
}
