package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.ByteArrayOutputStream
import java.io.File

private val kebabRegex = "-[a-zA-Z]".toRegex()

internal fun String.kebabToLowerCamelCase(): String {
    return kebabRegex.replace(this) {
        it.value.replace("-", "").toUpperCase()
    }
}

internal fun String.kebabToUpperCamelCase(): String {
    return kebabToLowerCamelCase().capitalize()
}

internal fun Task.createOutput() =
    project.serviceOf<StyledTextOutputFactory>().create(OpenApiSdkPlugin::class.qualifiedName)

fun Project.exec(vararg commandLine: String): String = exec(projectDir, *commandLine)

fun Project.exec(workingDir: File, vararg commandLine: String): String =
    ByteArrayOutputStream().use {
        project.exec {
            workingDir(workingDir)
            commandLine(*commandLine)
            isIgnoreExitValue = false
            standardOutput = it
        }
        it.toString().trimEnd()
    }
