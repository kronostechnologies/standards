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
import org.gradle.internal.logging.text.StyledTextOutput.Style.Normal

abstract class InitSdkTask : GitTask() {
    @get:Optional
    @get:Input
    abstract val overwrite: Property<Boolean?>

    @get:OutputDirectory
    abstract val target: DirectoryProperty

    @TaskAction
    fun initSdk() {
        val directory = target.get().asFile

        val directoryCreated = directory.mkdirs()
        val sdkRepositoryExists = project
            .exec(directory, "git", "ls-remote", "-h", uri.get(), directory.path, displayResult = true)
            .lines()
            .first { it.trim().startsWith("ERROR: Repository not found.") }
            .isNotBlank()

        if (!sdkRepositoryExists && directoryCreated) {
            createRepository()
        } else {
            recreateRepository()
        }
    }

    private fun createRepository() {
        val directory = target.get().asFile

        createOutput().style(Normal).println("Create ${repoId.get()}")
        project.exec(directory, "git", "init", "-b", "master", "--template", "")
    }

    private fun recreateRepository() {
        val directory = target.get().asFile

        if (overwrite.getOrElse(true) == true) {
            project.delete(directory)
            directory.mkdirs()
            project.exec("git", "clone", uri.get(), directory.path)
        } else {
            createOutput()
                .style(Error).text("Repository at ${directory.path} is invalid but overwrite is set to 'false'.")
        }
    }
        /*
           ## Init
           1. Create GH repository. If exists: goto update
           2. (Re-) create git folder.
           3. git init
           4. Populate templates
           5. Push to origin HEAD

           ## Update
           1. sync repo task
           2. Create branch
           3. Populate templates
           4. Push branch
           5. Open PR
         */
}
