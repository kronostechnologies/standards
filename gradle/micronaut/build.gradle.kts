version = "0.2.0-SNAPSHOT"

dependencies {
    val kotlinVersion = "1.6.10"
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-allopen:$kotlinVersion")

    implementation("com.bmuschko:gradle-docker-plugin:7.2.0")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:3.2.2")
    implementation("org.graalvm.buildtools:native-gradle-plugin:0.9.10")

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
