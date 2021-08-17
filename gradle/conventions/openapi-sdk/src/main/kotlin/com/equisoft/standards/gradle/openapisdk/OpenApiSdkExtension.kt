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

abstract class GitInfo @Inject constructor(
    private val providerFactory: ProviderFactory,
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

    fun <T> ifEnabled(transformer: GitInfo.() -> T): Provider<T?> = providerFactory.provider {
        if (enable.get()) {
            transformer(this)
        } else {
            null
        }
    }
}

abstract class OpenApiSdkExtension @Inject constructor(
    layout: ProjectLayout,
    objectFactory: ObjectFactory,
    project: Project
) {
    val customResourcesDir: DirectoryProperty = objectFactory.directoryProperty().apply {
        convention(layout.buildDirectory.dir("sdk"))
        finalizeValueOnRead()
    }
    val openApiPropertiesFile: RegularFileProperty = objectFactory.fileProperty().apply {
        convention(layout.buildDirectory.file("openapi.properties"))
        finalizeValueOnRead()
    }
    val projectKey: Property<String?> = objectFactory.property(String::class.java).apply {
        convention(project.rootProject.name)
        finalizeValueOnRead()
    }
    val specFile: RegularFileProperty = objectFactory.fileProperty().apply {
        finalizeValueOnRead()
    }
    val swaggerVersion: Property<String> = objectFactory.property(String::class.java).apply {
        finalizeValueOnRead()
    }
    val outputDir: DirectoryProperty = objectFactory.directoryProperty().apply {
        convention(layout.buildDirectory.dir("sdk"))
        finalizeValueOnRead()
    }

    @get:Nested
    abstract val git: GitInfo

    open fun git(action: Action<in GitInfo>) = action.execute(git)

    fun generatorOutputDir(generatorName: String): Provider<String> =
        outputDir.dir(generatorName).map { it.asFile.path }

    fun generatorOutputDir(generatorName: Provider<String>): Provider<String> =
        outputDir.dir(generatorName).map { it.asFile.path }
}
