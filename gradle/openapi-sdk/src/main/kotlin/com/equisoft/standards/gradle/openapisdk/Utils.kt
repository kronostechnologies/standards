package com.equisoft.standards.gradle.openapisdk

import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.ByteArrayOutputStream
import java.util.Locale

private val kebabRegex = "-[a-zA-Z]".toRegex()

internal fun String.kebabToLowerCamelCase(): String {
    return kebabRegex.replace(this) {
        it.value.replace("-", "").uppercase()
    }
}

internal fun String.kebabToUpperCamelCase(): String = kebabToLowerCamelCase().capitalize()

internal fun String.capitalize(): String = replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(Locale.getDefault())
    } else {
        it.toString()
    }
}

internal fun Task.createOutput() =
    project.serviceOf<StyledTextOutputFactory>().create(OpenApiSdkPlugin::class.qualifiedName)

fun Project.exec(vararg commandLine: String, displayResult: Boolean = false): String = exec(
    workingDir = projectDir,
    displayResult = displayResult,
    commandLine = commandLine
)

/**
 * Execute a command line and returns the standard output.
 */
fun Project.exec(workingDir: Any, vararg commandLine: String, displayResult: Boolean = false): String =
    ByteArrayOutputStream().use {
        val output = if (displayResult) TeeOutputStream(it, System.out) else it

        project.exec {
            workingDir(workingDir)
            commandLine(*commandLine)
            isIgnoreExitValue = false
            standardOutput = output
        }
        it.toString().trimEnd()
    }
