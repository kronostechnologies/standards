import io.gitlab.arturbosch.detekt.extensions.DetektExtension

version = "1.2.0-SNAPSHOT"

val functionalTestImplementation = configurations
    .create("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

val detektVersion = "1.20.0"

dependencies {
    val kotlinVersion = "1.7.10"
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jmailen.gradle:kotlinter-gradle:3.10.0")

    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")

    implementation("com.pinterest.ktlint:ktlint-core:0.45.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    val junit5Version = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

gradlePlugin {
    plugins {
        create("kotlinStandardsPlugin") {
            id = "$group.kotlin"
            displayName = "Equisoft Kotlin Standards"
            description = "Kotlin standards at Equisoft"
            implementationClass = "com.equisoft.standards.kotlin.KotlinStandardsPlugin"
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
    gradlePlugin.testSourceSets(this)
}

configure<DetektExtension> {
    source = files(
        file("src/main/kotlin"),
        file("src/test/kotlin"),
        functionalTestSourceSet.allSource.files
    )
}

tasks {
    named("checkStatic") {
        dependsOn("detektFunctionalTest")
    }

    withType<Test> {
        useJUnitPlatform()
    }

    val functionalTest = register<Test>("functionalTest") {
        group = "verification"
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath
    }

    register("generateVersions") {
        doFirst {
            File(sourceSets.main.get().output.resourcesDir, "versions.txt").writeText("""
            detekt:${detektVersion}
        """.trimIndent())
        }
    }

    processResources {
        finalizedBy("generateVersions")
    }

    check {
        dependsOn(functionalTest)
    }
}
