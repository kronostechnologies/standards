import io.gitlab.arturbosch.detekt.extensions.DetektExtension

group = "com.equisoft.standards"
version = "0.2.0"

plugins {
    kotlin("jvm") version "1.3.71"

    id("java-gradle-plugin")

    id("com.gradle.plugin-publish") version "0.11.0"
    id("io.gitlab.arturbosch.detekt") version "1.8.0"
    id("org.jmailen.kotlinter") version "2.3.2"
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

    implementation("org.jmailen.gradle:kotlinter-gradle:2.3.2")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.8.0")

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

tasks {
    withType<Test> {
        useJUnitPlatform()
    }
}

if (project.hasProperty("local")) {
    apply(from = rootProject.file("./profiles/local.gradle.kts"))
}
