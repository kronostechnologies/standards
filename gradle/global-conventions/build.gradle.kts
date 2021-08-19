version = "0.0.7-SNAPSHOT"

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("net.linguica.gradle:maven-settings-plugin:0.5")
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
