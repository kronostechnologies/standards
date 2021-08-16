package com.equisoft.standards.gradle.openapisdk

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

abstract class OpenApiSdkExtension(layout: ProjectLayout) {
    abstract val customResourcesDir: DirectoryProperty
    abstract val openApiPropertiesFile: RegularFileProperty
    abstract val outputDir: DirectoryProperty
    abstract val specFile: RegularFileProperty
    abstract val swaggerVersion: Property<String?>

    init {
        outputDir
            .convention(layout.buildDirectory.dir("sdk"))
            .finalizeValueOnRead()
        customResourcesDir
            .convention(layout.projectDirectory.dir("build-resources/openapi-generator"))
            .finalizeValueOnRead()
        openApiPropertiesFile
            .convention(layout.buildDirectory.file("openapi.properties"))
            .finalizeValueOnRead()
        specFile.finalizeValueOnRead()
        swaggerVersion.finalizeValueOnRead()
    }

    fun generatorOutputDir(generatorName: String): Provider<String> =
        outputDir.dir(generatorName).map { it.asFile.path }

    fun generatorOutputDir(generatorName: Provider<String>): Provider<String> =
        outputDir.dir(generatorName).map { it.asFile.path }

    companion object {
        const val PLUGIN_GROUP = "OpenAPI SDK"
    }
}
