import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jmailen.gradle.kotlinter.KotlinterExtension

group = "com.equisoft.standards"
version = "0.5.0"

plugins {
    kotlin("jvm") version "1.4.10"

    id("java-gradle-plugin")

    id("com.gradle.plugin-publish") version "0.11.0"
    id("io.gitlab.arturbosch.detekt") version "1.13.1"
    id("org.jmailen.kotlinter") version "3.2.0"
}

repositories {
    jcenter()
    gradlePluginPortal()
}

val functionalTestImplementation = configurations
    .create("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
    implementation("org.jmailen.gradle:kotlinter-gradle:3.2.0")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.13.1")

    // Custom rule dependency because of https://github.com/pinterest/ktlint/issues/764
    implementation("com.pinterest.ktlint:ktlint-core:0.39.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    val junit5Version = "5.6.2"
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
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val checkStatic by tasks.creating(Task::class) {
    dependsOn("lintKotlin")
    dependsOn("detekt")
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

configure<KotlinterExtension> {
    disabledRules = arrayOf("indent") // https://github.com/pinterest/ktlint/issues/764
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

if (project.hasProperty("local")) {
    apply(from = rootProject.file("./profiles/local.gradle.kts"))
}
