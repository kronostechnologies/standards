package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.createOutput
import com.equisoft.standards.gradle.openapisdk.exec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.text.StyledTextOutput.Style.Info
import org.gradle.internal.logging.text.StyledTextOutput.Style.Normal

abstract class PublishSdkTask : DefaultTask() {
    @get:InputDirectory
    abstract val directory: DirectoryProperty

    @TaskAction
    fun publish() {
        val repoHandle = project.exec("git", "remote", "get-url", "origin")
            .replace(Regex(""".+@.+:([\w\d.-]+/[\w\d.-]+)\.git"""), "$1")
        val lastCommit = project.exec("git", "rev-parse", "--short", "HEAD")
        val commitMessage = "chore: update generated code from $repoHandle@$lastCommit"

        createOutput()
            .style(Normal).println("You can publish the SDK files by running:")
            .style(Info)
            .println("  cd \"${directory.get().asFile.path}\"")
            .println("    && git add -A")
            .println("    && git commit -m \"$commitMessage\"")
            .println("    && git push origin HEAD")
    }
}
