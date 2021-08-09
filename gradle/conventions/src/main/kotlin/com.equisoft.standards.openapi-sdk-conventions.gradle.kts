import org.gradle.internal.logging.text.StyledTextOutput
import org.gradle.internal.logging.text.StyledTextOutputFactory
import org.gradle.kotlin.dsl.support.serviceOf
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import org.openapitools.generator.gradle.plugin.tasks.ValidateTask

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
}

val openApiSdk = extensions.create<OpenApiSdkExtension>("openApiSdk", layout)

afterEvaluate {
    val isMicronautResolved = configurations.any { configuration ->
        configuration.dependencies.any {
            // We don't have to guess versions if the Micronaut's BOM is resolvable
            it.group == "io.micronaut" && it.name == "micronaut-bom"
        }
    }

    if (isMicronautResolved) {
        configureMicronautOpenApi()
        pluginManager.withPlugin("org.jetbrains.kotlin.kapt") {
            associateMicronautOpenApiToKapt()
        }
    }
}

fun configureMicronautOpenApi() {
    dependencies {
        // micronaut-bom defines swagger-annotations but we allow overriding the version
        val swaggerVersionQualifier = openApiSdk.swaggerVersion.map { ":${it}" }.orElse("").get()
        "implementation"("io.swagger.core.v3:swagger-annotations$swaggerVersionQualifier")
    }

    tasks {
        register<WriteProperties>("generateOpenapiProperties") {
            outputFile = openApiSdk.openApiPropertiesFile.get().asFile

            property("micronaut.openapi.expand.api.version", version)
            property("mapping.path", "docs/openapi")
            property("rapidoc.enabled", "true")
            property("redoc.enabled", "true")
            property("swagger-ui.enabled", "true")
        }
    }
}

fun associateMicronautOpenApiToKapt() {
    dependencies {
        "kapt"("io.micronaut.openapi:micronaut-openapi")
    }

    // TODO: Can't seem to configure it properly without having Kotlin in the classpath.
    //  KAPT is optional here and we don't want it to be transitive.
    //  With `compileOnly` it compiles but then we get a NoClassDefFound error at runtime even if kapt is available.
    //  Genius
    // extensions.configure<KaptExtension> {
    //     arguments {
    //         arg("micronaut.openapi.config.file", openApiSdk.openApiPropertiesFile.asFile.get().path)
    //     }
    // }

    tasks {
        matching {
            it.name == "kaptKotlin"
        }.all {
            dependsOn("generateOpenapiProperties")

            inputs.file(openApiSdk.openApiPropertiesFile)
                .withPropertyName("openApiProperties")
                .withPathSensitivity(PathSensitivity.RELATIVE)
        }

        withType<ValidateTask> {
            dependsOn("kaptKotlin")
        }

        withType<GenerateTask> {
            dependsOn("kaptKotlin")
        }
    }
}


plugins {
    id("org.openapi.generator")
}

tasks {
    named("check") {
        dependsOn(openApiValidate)
    }

    withType<ValidateTask> {
        inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
    }

    withType<GenerateTask> {
        inputSpec.set(openApiSdk.specFile.map { it.asFile.path })
        outputDir.set(openApiSdk.outputDir.dir(generatorName).map { it.asFile.path })

        id.set("${rootProject.name}-sdk")
        groupId.set("${project.group}.sdk")
        version.set("${project.version}")

        templateDir.set(provider {
            val file = openApiSdk.customResourcesDir.dir(generatorName).map { it.asFile }.orNull
            if (file?.exists() == true) {
                file.path
            } else {
                null
            }
        })
    }

    register<GenerateTask>("generateTypeScriptSdk") {
        generatorName.set("typescript-fetch")
        configOptions.set(provider {
            mapOf(
                "authorName" to "Equisoft Inc.",
                "enumPropertyNaming" to "original",
                "npmName" to "@equisoft/${id.get()}", // npmName is required for the project's structure to be generated (src/, ...)
                "supportsES6" to "true",
                "typescriptThreePlus" to "true",
                "variableNamingConvention" to "camelCase",
            )
        })

        doFirst {
            val path = outputDir.get()
            delete("$path/src", "$path/docs", "$path/dist")
        }

        doLast {
            val path = outputDir.get()
            val output = project.serviceOf<StyledTextOutputFactory>().create("an-output")

            output
                .style(StyledTextOutput.Style.Header)
                .println("Building SDK")

            exec {
                workingDir(path)
                commandLine("yarn", "install", "--immutable")
            }
            exec {
                workingDir(path)
                commandLine("yarn", "build")
            }

            output
                .style(StyledTextOutput.Style.Success)
                .println("Typescript SDK generated to: $path")
        }
    }
}
