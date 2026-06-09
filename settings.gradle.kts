pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven("https://maven.fabricmc.net")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.kikugie.dev/releases")
        maven("https://jitpack.io/")
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.architectury.dev")
        maven("https://repo.polyfrost.org/releases")
        maven("https://repo.polyfrost.org/snapshots")

        mavenLocal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.5"
    id("dev.kikugie.loom-back-compat") version "0.3"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val versions = listOf("1.21.1", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1.2")
stonecutter {
    create(rootProject) {
        versions(versions)
        vcsVersion = versions.last()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "CrashPatch"
