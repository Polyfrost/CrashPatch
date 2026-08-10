pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.architectury.dev")
        maven("https://repo.polyfrost.org/releases")
        maven("https://repo.polyfrost.org/snapshots")
        maven("https://jitpack.io/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
    id("dev.kikugie.loom-back-compat") version "0.4.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// 26.1 builds against the 26.1.2 dev jar: NEC 4.4.9+26.1.2 refuses anything older.
val versionOverrides = mapOf(
    "26.1" to "26.1.2"
)

stonecutter {
    create(rootProject) {
        for (ver in listOf("1.21.1", "1.21.4", "1.21.5", "1.21.8", "1.21.10", "1.21.11", "26.1", "26.2")) {
            version(ver, versionOverrides[ver] ?: ver)
        }

        vcsVersion = "26.2"
    }
}

rootProject.name = "CrashPatch"
