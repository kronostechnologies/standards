package com.equisoft.standards.gradle.micronaut

import org.gradle.api.JavaVersion
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

@SuppressWarnings("MagicNumber", "UnnecessaryAbstractClass")
abstract class MicronautSettingsExtension @Inject constructor(
    objectFactory: ObjectFactory
) {
    val javaVersion: Property<JavaVersion> = objectFactory.property(JavaVersion::class.java).apply {
        convention(JavaVersion.current())
    }
    val exposedPorts: ListProperty<Int> = objectFactory.listProperty(Int::class.java).apply {
        convention(listOf(8080, 8081))
    }
    val projectRootPackage: Property<String> = objectFactory.property(String::class.java).apply {
    }
    val runtimeImage: Property<String> = objectFactory.property(String::class.java).apply {
        convention("gcr.io/distroless/cc-debian11:nonroot")
    }
}
