import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jmailen.gradle.kotlinter.KotlinterExtension

group = "com.equisoft.standards"
version = "0.5.0"

plugins {
    kotlin("jvm") version "1.4.32"

    id("java-gradle-plugin")
    id("maven-publish")

    id("com.github.ben-manes.versions") version "0.38.0"
    id("com.gradle.plugin-publish") version "0.14.0"
    id("io.gitlab.arturbosch.detekt") version "1.17.0"
    id("org.jmailen.kotlinter") version "3.4.4"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val functionalTestImplementation = configurations
    .create("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.4.32"))
    kotlin("kotlin-stdlib-jdk8")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    implementation("org.jmailen.gradle:kotlinter-gradle:3.4.4")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.17.0")

    // Custom rule dependency because of https://github.com/pinterest/ktlint/issues/764
    implementation("com.pinterest.ktlint:ktlint-core:0.41.0")

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

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "1.8"
}

configure<KotlinterExtension> {
    disabledRules = arrayOf("indent") // https://github.com/pinterest/ktlint/issues/764
}

tasks {
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

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}

if (project.hasProperty("local")) {
    apply(from = rootProject.file("./profiles/local.gradle.kts"))
}
