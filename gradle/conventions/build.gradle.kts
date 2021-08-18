group = "com.equisoft.standards"

subprojects {
    group = "com.equisoft.standards"
    version = "0.0.7-SNAPSHOT"

    apply(plugin = "maven-publish")

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "equisoft-standards"
                url = uri("https://maven.pkg.github.com/kronostechnologies/standards")
                credentials {
                    name = "gprWrite"
                    username = project.findProperty("gpr.write.user")?.toString()
                        ?: System.getenv("GPR_USER")
                        ?: System.getenv("GHCR_USER")
                    password = project.findProperty("gpr.write.key")?.toString()
                        ?: System.getenv("GPR_TOKEN")
                        ?: System.getenv("GHCR_TOKEN")
                }
            }
        }
    }

    tasks {
        named("publish") {
            dependsOn("check")
        }
    }
}
