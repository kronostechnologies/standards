package com.equisoft.standards.gradle.openapisdk.generators

import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

internal fun GenerateTask.configureGradleSdkPrePostTasks() {
    doFirst {
        val path = outputDir.get()
        project.delete("$path/src", "$path/docs", "$path/build")
    }

    doLast {
        val path = outputDir.get()

        project.exec {
            workingDir(path)
            commandLine("chmod", "+x", "gradlew")
        }
        project.exec {
            workingDir(path)
            commandLine("./gradlew", "build")
        }
    }
}
