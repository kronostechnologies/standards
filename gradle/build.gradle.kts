plugins {
    id("net.linguica.maven-settings") version "0.5"
}

subprojects {
    group = "com.equisoft.standards"

    apply(plugin = "net.linguica.maven-settings")

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        distributionSha256Sum = "9bb8bc05f562f2d42bdf1ba8db62f6b6fa1c3bf6c392228802cc7cb0578fe7e0"
        gradleVersion = "7.1.1"
    }
}
