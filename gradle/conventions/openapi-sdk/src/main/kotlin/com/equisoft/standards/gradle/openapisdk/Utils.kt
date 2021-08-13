package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Task
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf

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
