version = "0.0.8"

dependencies {
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("net.linguica.gradle:maven-settings-plugin:0.5")
    implementation("org.owasp:dependency-check-gradle:6.4.1.1")
    implementation("org.owasp:dependency-check-core:6.4.1")
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
