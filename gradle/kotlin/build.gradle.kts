import io.gitlab.arturbosch.detekt.extensions.DetektExtension

version = "0.6.0-SNAPSHOT"

val functionalTestImplementation = configurations
    .create("functionalTestImplementation")
    .extendsFrom(configurations.getByName("testImplementation"))

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.5.21"))

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
    input = files(
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

    check {
        dependsOn(functionalTest)
    }
}
