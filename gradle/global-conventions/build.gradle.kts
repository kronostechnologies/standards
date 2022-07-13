version = "1.2.0-SNAPSHOT"

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("org.cyclonedx:cyclonedx-gradle-plugin:1.7.0")
}

gradlePlugin {
    plugins {
        register("globalConventions") {
            id = "$group.global-conventions"
            displayName = "OpenAPI SDK conventions plugin"
            description = "A plugin to common conventions"
            implementationClass = "com.equisoft.standards.gradle.global.GlobalConventionsPlugin"
        }
    }
}
