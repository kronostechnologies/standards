# Gradle Plugin

## Usage

`build.gradle.kts`:
```kotlin
plugins {
    id("com.equisoft.kotlin-standards") version "0.1.0"
}
```

## Publishing

1. [Create a Gradle account](https://guides.gradle.org/publishing-plugins-to-gradle-plugin-portal/) 
1. Create a new branch: `git checkout -b release/kotlin-standards-1.2.3`
1. Bump version of `./build.gradle.kts`
1. Update the `CHANGELOG.md` file to reflect the changes to be published
1. Commit and create a tag: `git tag kotlin-standards-1.2.3`
1. Push branch and tag: `git push origin HEAD; git push origin kotlin-standards-1.2.3`
1. Open a Pull Request on [GitHub](https://github.com/kronostechnologies/standards)
1. Publish once approved: `make publish`
