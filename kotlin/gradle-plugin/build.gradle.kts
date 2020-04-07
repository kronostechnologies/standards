import io.gitlab.arturbosch.detekt.extensions.DetektExtension

group = "com.equisoft"
version = "0.1.0"

repositories {
    jcenter()
    gradlePluginPortal()
}

plugins {
    kotlin("jvm") version "1.3.71"

    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.11.0"

    id("org.jmailen.kotlinter") version "2.3.2"
    id("io.gitlab.arturbosch.detekt") version "1.8.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())

    implementation("org.jmailen.gradle:kotlinter-gradle:2.3.2")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.8.0")
}

pluginBundle {
    website = "https://www.github.com/kronostechnologies/standards"
    vcsUrl = "https://www.github.com/kronostechnologies/standards"
    description = "Kotlin standards at Equisoft"
    tags = listOf("equisoft", "kotlin", "standards")
}

gradlePlugin {
    plugins {
        create("kotlinStandards") {
            id = "com.equisoft.kotlin-standards"
            displayName = "Equisoft Kotlin Standards"
            description = "Kotlin standards at Equisoft"
            version = version
            implementationClass = "com.equisoft.standards.kotlin.gradleplugin.KotlinStandardsPlugin"
        }
    }
}

configure<DetektExtension> {
    config = files(file("src/main/resources/detekt.yml"))
    input = files(file("src/main/kotlin"), file("src/test/kotlin"))
}
