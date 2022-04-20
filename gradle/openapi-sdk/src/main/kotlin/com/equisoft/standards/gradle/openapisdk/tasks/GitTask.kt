package com.equisoft.standards.gradle.openapisdk.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional

abstract class GitTask : DefaultTask() {
    @get:Input
    abstract val host: Property<String>

    @get:Input
    abstract val userId: Property<String>

    @get:Optional
    @get:Input
    abstract val token: Property<String>

    @get:Input
    abstract val repoId: Property<String>

    @get:Internal
    val uri: Provider<String> = host.map {
        if (token.isPresent) {
            "https://${token.get()}@$it/${userId.get()}/${repoId.get()}.git"
        } else {
            "git@$it:${userId.get()}/${repoId.get()}.git"
        }
    }
}
