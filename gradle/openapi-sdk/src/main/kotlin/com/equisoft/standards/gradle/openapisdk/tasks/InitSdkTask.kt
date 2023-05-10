package com.equisoft.standards.gradle.openapisdk.tasks

import com.equisoft.standards.gradle.openapisdk.createOutput
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class InitSdkTask : GitTask() {

    @get:OutputDirectory
    abstract val directory: DirectoryProperty

    @TaskAction
    fun initSdk() {
        val directory = directory.get().asFile
        val output = createOutput()

        val sdkRepositoryExists = project
            .exec(directory, "git", "ls-remote", "-h", uri.get(), directory.path, displayResult = true)
            .line()
            .first{
                it.trim().startsWith("ERROR: Repository not found.")
            }
        val directoryCreated = directory.mkdirs().isBlank();

        if (!sdkRepositoryExists) {
            if (directoryCreated) {
                output.style(Normal).println("Create $sdkRepository")
                directory.project.exec(directory, "git", "init", "-b", "master", "--template", "")
            } else {
                // directory already exists on filesystem but there is no repo on github. What do we do?
            }
        } else {
            project.delete(directory)
            directory.mkdirs()
            project.exec("git", "clone", uri.get(), directory.path)
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
}
