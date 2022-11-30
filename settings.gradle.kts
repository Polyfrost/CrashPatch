pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.polyfrost.cc/releases")
    }
    plugins {
        val egtVersion = "0.1.23"
        id("cc.polyfrost.multi-version.root") version egtVersion
    }
}

val mod_name: String by settings

rootProject.name = mod_name
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.8.9-forge"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}