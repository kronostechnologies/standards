# Kotlin Standards

## Packages

- `gradle-plugin`: a gradle plugin wrapping Kotlinter and Detekt.
- `consumer`: for development and documentation purposes. 

## Todo

### Multi-project build
`consumer` is including the `gradle-plugin` build directly (`consumer/settings.gradle.kts`),
which is not compatible with a multi-project layout. We might publish locally with `maven-publish` plugin. 
Example [here](https://github.com/gradle/kotlin-dsl-samples/tree/master/samples/gradle-plugin).
