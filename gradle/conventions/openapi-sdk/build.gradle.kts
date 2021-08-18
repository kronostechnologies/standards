import org.apache.tools.ant.taskdefs.Patch

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

val openApiPatchesSourceDirectory = layout.projectDirectory.dir("src/patches")
val openApiTemplatesDirectory = layout.buildDirectory.dir("tmp/openapi-templates")
val openApiPatchedTargetDirectory = layout.buildDirectory.dir("resources/patches")

val openApiGenerator by configurations.creating {
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    val openApiVersion = "5.2.0"

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    implementation("org.openapitools:openapi-generator-gradle-plugin:$openApiVersion")
    implementation("com.equisoft.openapi.generator.micronaut:micronaut-project-openapi-generator:0.1.0")

    openApiGenerator("org.openapitools:openapi-generator:$openApiVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_13
}

gradlePlugin {
    plugins {
        register("openApiSdk") {
            id = "$group.openapi-sdk"
            displayName = "OpenAPI SDK conventions plugin"
            description = "A conventional plugin to configure SDK generation"
            implementationClass = "com.equisoft.standards.gradle.openapisdk.OpenApiSdkPlugin"
        }
    }
}

tasks {
    processResources {
        dependsOn("patchTemplates")
    }

    register<Sync>("syncTemplates") {
        dependsOn(openApiGenerator)

        from(zipTree(openApiGenerator.singleFile))
        exclude(
            "META-INF/**",
            "codegen/**",
            "org/**",
        )
        into(openApiTemplatesDirectory)
    }

    register("applyPatches") {
        dependsOn("syncTemplates")

        inputs.dir(openApiPatchesSourceDirectory)
        outputs.dir(openApiPatchedTargetDirectory)

        doLast {
            fileTree(openApiPatchesSourceDirectory)
                .filter { it.extension == "patch" }
                .forEach(::applyPatch)
        }
    }

    register<Copy>("patchTemplates") {
        dependsOn("applyPatches")

        from(fileTree(openApiPatchedTargetDirectory))
        into(layout.buildDirectory.dir("resources/main"))
    }
}

fun applyPatch(patch: File) {
    val parentName = patch.parentFile.name
    val templateName = patch.nameWithoutExtension
    val originalFile = openApiTemplatesDirectory.get().dir(parentName).dir(templateName).asFile
    val targetFile = openApiPatchedTargetDirectory.get().dir(parentName).dir(templateName).asFile

    val targetDirectory = targetFile.parentFile
    if (!targetDirectory.exists()) {
        targetDirectory.mkdirs()
    }

    Patch().apply {
        setPatchfile(patch)
        setOriginalfile(originalFile)
        setDestfile(targetFile)
        setDir(layout.buildDirectory.asFile.get())
        setStrip(0)
        setFailOnError(true)
        execute()
    }
}
