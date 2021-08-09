import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

group = "com.equisoft.standards"
version = "0.6.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.5.21"

    id("java-gradle-plugin")
    id("maven-publish")

    id("com.github.ben-manes.versions") version "0.39.0"
    id("com.gradle.plugin-publish") version "0.15.0"
    id("io.gitlab.arturbosch.detekt") version "1.17.1"
    id("org.jmailen.kotlinter") version "3.4.5"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val functionalTestImplementation = configurations
    .create("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.21"))
    kotlin("stdlib-jdk8")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    implementation("org.jmailen.gradle:kotlinter-gradle:3.4.5")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.17.1")

    implementation("com.pinterest.ktlint:ktlint-core:0.42.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    val junit5Version = "5.7.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

pluginBundle {
    website = "https://www.github.com/kronostechnologies/standards"
    vcsUrl = "https://www.github.com/kronostechnologies/standards.git"
    tags = listOf("equisoft", "kotlin", "lint", "check")
}

gradlePlugin {
    plugins {
        create("kotlinStandardsPlugin") {
            id = "com.equisoft.standards.kotlin"
            displayName = "Equisoft Kotlin Standards"
            description = "Kotlin standards at Equisoft"
            implementationClass = "com.equisoft.standards.kotlin.KotlinStandardsPlugin"
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
    gradlePlugin.testSourceSets(this)
}

val functionalTest by tasks.creating(Test::class) {
    group = "verification"
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val checkStatic by tasks.creating(Task::class) {
    group = "verification"
    dependsOn("lintKotlin")
    dependsOn("detektMain")
    dependsOn("detektTest")
    dependsOn("detektFunctionalTest")
}

val check by tasks.getting(Task::class) {
    dependsOn(functionalTest)
}

configure<DetektExtension> {
    config = files(file("src/main/resources/detekt.yml"))
    input = files(
        file("src/main/kotlin"),
        file("src/test/kotlin"),
        functionalTestSourceSet.allSource.files
    )
}

tasks {
    withType<Detekt>().configureEach {
        jvmTarget = "1.8"
    }

    withType<Test> {
        useJUnitPlatform()
    }

    dependencyUpdates {
        fun isStable(version: String): Boolean {
            val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
            val regex = "^[0-9,.v-]+(-r)?$".toRegex()
            return stableKeyword || regex.matches(version)
        }

        checkConstraints = true
        gradleReleaseChannel = "current"
        outputFormatter = "json,html"

        rejectVersionIf {
            !isStable(candidate.version) && isStable(currentVersion)
        }
    }
}

if (project.hasProperty("local")) {
    apply(from = rootProject.file("./profiles/local.gradle.kts"))
}
