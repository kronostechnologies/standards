version = "0.4.0-SNAPSHOT"

dependencies {
    val kotlinVersion = "1.6.21"
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    val micronautPluginVersion = "3.3.2"
    implementation("io.micronaut.gradle:micronaut-docker-plugin:$micronautPluginVersion")
    implementation("io.micronaut.gradle:micronaut-graalvm-plugin:$micronautPluginVersion")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:$micronautPluginVersion")
    implementation("io.micronaut.gradle:micronaut-minimal-plugin:$micronautPluginVersion")
    implementation("com.bmuschko:gradle-docker-plugin:7.3.0")
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.9.11")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    val junit5Version = "5.8.2"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5Version")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junit5Version")
}

gradlePlugin {
    plugins {
        create("equisoftMicronautPlugin") {
            id = "$group.micronaut"
            displayName = "Equisoft Micronaut Plugin"
            description = "Equisoft Micronaut Plugin"
            implementationClass = "com.equisoft.standards.gradle.micronaut.MicronautPlugin"
        }
    }
}