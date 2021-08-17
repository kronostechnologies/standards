package com.equisoft.standards.gradle.openapisdk

import org.apache.commons.io.output.TeeOutputStream
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf
import java.io.ByteArrayOutputStream

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

/**
 * Execute a command line and returns the standard output.
 *
 * @see Project.exec
 */
fun Project.exec(workingDir: Any, vararg commandLine: String): String =
    ByteArrayOutputStream().use {
        project.exec {
            workingDir(workingDir)
            commandLine(*commandLine)
            isIgnoreExitValue = false
            standardOutput = TeeOutputStream(it, System.out)
        }
        it.toString().trimEnd()
    }
