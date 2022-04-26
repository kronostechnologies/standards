package com.equisoft.standards.gradle.micronaut

import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import io.micronaut.gradle.MicronautApplicationPlugin
import io.micronaut.gradle.MicronautExtension
import io.micronaut.gradle.MicronautRuntime.NETTY
import io.micronaut.gradle.MicronautTestRuntime.JUNIT_5
import io.micronaut.gradle.docker.MicronautDockerfile
import io.micronaut.gradle.docker.NativeImageDockerfile
import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.provider.DefaultProvider
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.allopen.gradle.AllOpenGradleSubplugin

class MicronautPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val micronautSettingsExtension = extensions.create<MicronautSettingsExtension>("micronautSettings")

        plugins.apply(MicronautApplicationPlugin::class.java)
        plugins.apply(AllOpenGradleSubplugin::class.java)

        configureExtensions(micronautSettingsExtension)
        configureTasks(micronautSettingsExtension)
        configureAnnotationProcessing()
    }

    private fun Project.configureExtensions(micronautSettingsExtension: MicronautSettingsExtension) {
        configureMicronautExtensionDefaultValues(micronautSettingsExtension)
        configureDockerNativeImageDefaultValues(micronautSettingsExtension)
    }

    @SuppressWarnings("LongMethod", "MaxLineLength")
    private fun Project.configureTasks(
        micronautSettingsExtension: MicronautSettingsExtension
    ) {
        tasks {
            fun configureDockerfile(dockerfile: Dockerfile) = with(dockerfile) {
                val applicationVersion = version.toString()
                environmentVariable("APPLICATION_VERSION", applicationVersion)
                label(mapOf("org.opencontainers.image.version" to applicationVersion))
            }

            withType<MicronautDockerfile> {
                baseImage.set(
                    micronautSettingsExtension.javaVersion.map {
                        "eclipse-temurin:${micronautSettingsExtension.javaVersion.get()}-jre"
                    }
                )
                runCommand("useradd app && chown app -R /home/app")
                user("app")
                instruction("""HEALTHCHECK --interval=10s --timeout=3s --start-period=10s CMD curl --fail http://127.0.0.1:8081/health | grep '"status":"UP"' """) // ktlint-disable max-line-length
                exposedPorts.set(micronautSettingsExtension.exposedPorts)
                configureDockerfile(this)
            }

            withType<NativeImageDockerfile> {
                baseImage.set(micronautSettingsExtension.runtimeImage)
                exposedPorts.set(micronautSettingsExtension.exposedPorts)
                configureDockerfile(this)
            }

            withType<DockerBuildImage> {
                val registry = findProperty("application.docker.registry")
                val imageName = findProperty("application.docker.imageName") ?: "${rootProject.name}-${project.name}"
                val fullImageName = listOfNotNull(registry, imageName).joinToString("/").toLowerCase()
                images.set(listOf("$fullImageName:$version"))
            }

            withType<JavaExec> {
                jvmArgs("-XX:TieredStopAtLevel=1")
                systemProperty("micronaut.environments", "development")
            }
        }
    }

    private fun Project.configureMicronautExtensionDefaultValues(
        micronautSettingsExtension: MicronautSettingsExtension
    ) {
        extensions.configure<MicronautExtension> {
            enableNativeImage.set(true)
            runtime.set(NETTY)
            testRuntime.set(JUNIT_5)

            processing {
                incremental(true)
                annotations.add(DefaultProvider { "${micronautSettingsExtension.projectRootPackage.get()}.*" })
            }
        }
    }

    @SuppressWarnings("LongMethod")
    private fun Project.configureDockerNativeImageDefaultValues(
        micronautSettingsExtension: MicronautSettingsExtension
    ) {
        extensions.configure(GraalVMExtension::class.java) {
            testSupport.set(false)

            binaries {
                named("main") {
                    val xmx = findProperty("application.docker.graalMaxHeap") ?: "5G"

                    buildArgs.add("-J-Xmx$xmx")
                    buildArgs.add("-J-Djdk.lang.Process.launchMechanism=vfork")
                    buildArgs.add("--static")
                    buildArgs.add("--libc=glibc")
                    buildArgs.add("-H:+StaticExecutableWithDynamicLibC")
                    buildArgs.add("-H:+ReportExceptionStackTraces")
                    buildArgs.add("-R:-WriteableCodeCache")
                    fallback.set(false)
                    sharedLibrary.set(false)
                    javaLauncher.set(
                        javaToolchains.launcherFor {
                            languageVersion.set(
                                micronautSettingsExtension.javaVersion.map { JavaLanguageVersion.of(it.majorVersion) }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun Project.configureAnnotationProcessing() {
        val sourceSets = convention
            .getPlugin(JavaPluginConvention::class.java)
            .sourceSets
        addGraalVMAnnotationProcessorDependency(sourceSets.filter { sourceSet -> SOURCE_SETS.contains(sourceSet.name) })
    }

    private fun Project.addGraalVMAnnotationProcessorDependency(sourceSets: Iterable<SourceSet>) {
        CONFIGURATIONS.forEach {
            configurations.findByName(it)?.run {
                for (sourceSet in sourceSets) {
                    this@addGraalVMAnnotationProcessorDependency.dependencies.add(
                        this.name,
                        "io.micronaut:micronaut-graal"
                    )
                }
            }
        }
    }

    companion object {
        private val SOURCE_SETS: Set<String> = setOf("main", "test")
        private val CONFIGURATIONS: Set<String> = setOf("kapt", "kaptTest")
    }
}

val Project.javaToolchains: JavaToolchainService get() = extensions.getByName("javaToolchains") as JavaToolchainService
