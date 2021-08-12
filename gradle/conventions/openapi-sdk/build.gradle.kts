plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    implementation("org.openapitools:openapi-generator-gradle-plugin:5.2.0")
}

gradlePlugin {
    plugins {
        register("openApiSdk") {
            id = "$group.openapi-sdk"
            displayName = "OpenAPI SDK conventions plugin"
            description = "A conventional plugin to configure SDK generation"
            implementationClass = "com.equisoft.standards.gradle.openapisdk.OpenApiSdkPlugin"
        }
    }
}
