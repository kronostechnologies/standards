package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Nested
import javax.inject.Inject

abstract class OpenApiSdkExtension @Inject constructor(
    layout: ProjectLayout,
    objectFactory: ObjectFactory,
    project: Project,
    private val providerFactory: ProviderFactory
) {
    val openApiPropertiesFile: RegularFileProperty = objectFactory.fileProperty().apply {
        convention(layout.buildDirectory.file("openapi.properties"))
        finalizeValueOnRead()
    }
    val outputDir: DirectoryProperty = objectFactory.directoryProperty().apply {
        convention(layout.buildDirectory.dir("sdk"))
        finalizeValueOnRead()
    }
    val projectKey: Property<String> = objectFactory.property(String::class.java).apply {
        convention(project.rootProject.name)
        finalizeValueOnRead()
    }
    val specFile: RegularFileProperty = objectFactory.fileProperty().apply {
        finalizeValueOnRead()
    }
    val swaggerVersion: Property<String> = objectFactory.property(String::class.java).apply {
        finalizeValueOnRead()
    }

    @get:Nested
    abstract val git: GitInfo

    fun git(action: Action<in GitInfo>) = action.execute(git)

    fun <T> withGitEnabled(transformer: GitInfo.() -> T): Provider<T?> = providerFactory.provider {
        if (git.enable.get()) {
            transformer(git)
        } else {
            null
        }
    }
}

open class GitInfo @Inject constructor(
    objectFactory: ObjectFactory
) {
    val enable: Property<Boolean> = objectFactory.property(Boolean::class.java).apply {
        convention(false)
        finalizeValueOnRead()
    }
    val host: Property<String> = objectFactory.property(String::class.java).apply {
        convention("github.com")
        finalizeValueOnRead()
    }
    val userId: Property<String?> = objectFactory.property(String::class.java).apply {
        finalizeValueOnRead()
    }
    val token: Property<String?> = objectFactory.property(String::class.java).apply {
        finalizeValueOnRead()
    }
}
