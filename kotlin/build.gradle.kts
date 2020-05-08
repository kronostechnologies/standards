import io.gitlab.arturbosch.detekt.extensions.DetektExtension

group = "com.equisoft.standards"
version = "0.1.0"

plugins {
    kotlin("jvm") version "1.3.71"

    id("java-gradle-plugin")

    id("org.jmailen.kotlinter") version "2.3.2"
    id("io.gitlab.arturbosch.detekt") version "1.8.0"
}

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.jmailen.gradle:kotlinter-gradle:2.3.2")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.8.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

//pluginBundle {
//    website = "https://www.github.com/kronostechnologies/standards"
//    vcsUrl = "https://www.github.com/kronostechnologies/standards"
//    description = "Kotlin standards at Equisoft"
//    tags = listOf("equisoft", "kotlin", "standards")
//}

gradlePlugin {
    plugins {
        create("kotlinStandards") {
            id = "com.equisoft.standards.kotlin"
            displayName = "Equisoft Kotlin Standards"
            description = "Kotlin standards at Equisoft"
            version = version
            implementationClass = "com.equisoft.standards.kotlin.KotlinStandardsPlugin"
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations
    .getByName("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

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
    input = files(file("src/main/kotlin"), file("src/test/kotlin"))
}
