package com.equisoft.standards.kotlin

object VersionsCatalog {
    private val versions = mutableMapOf<String, String>()

    init {
        this::class.java.getResourceAsStream("/versions.txt")?.run {
            bufferedReader().readLines().map {
                val (key, version) = it.split(":")
                versions.put(key, version)
            }
        }
    }

    fun get(dependencyKey: String): String? = versions[dependencyKey]
}
