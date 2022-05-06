version = "1.0.0"

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
    implementation("net.linguica.gradle:maven-settings-plugin:0.5")
    implementation("com.cyclonedx:cyclonedx-gradle-plugin:1.5.0")
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
