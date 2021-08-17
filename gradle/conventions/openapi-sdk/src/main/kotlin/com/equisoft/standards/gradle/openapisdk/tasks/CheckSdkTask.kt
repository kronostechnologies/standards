package com.equisoft.standards.gradle.openapisdk.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory

abstract class CheckSdkTask : DefaultTask() {
    @get:InputDirectory
    abstract val directory: DirectoryProperty
}
