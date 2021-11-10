package com.equisoft.standards.gradle.openapisdk.tasks

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
