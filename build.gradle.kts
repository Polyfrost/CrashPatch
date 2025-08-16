@file:Suppress("UnstableApiUsage", "PropertyName")

import dev.deftu.gradle.utils.GameSide
import dev.deftu.gradle.utils.includeOrShade
import dev.deftu.gradle.utils.version.MinecraftVersion
import dev.deftu.gradle.utils.version.MinecraftVersions

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion") // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
    id("dev.deftu.gradle.tools") // Applies several configurations to things such as the Java version, project name/version, etc.
    id("dev.deftu.gradle.tools.resources") // Applies resource processing so that we can replace tokens, such as our mod name/version, in our resources.
    id("dev.deftu.gradle.tools.bloom") // Applies the Bloom plugin, which allows us to replace tokens in our source files, such as being able to use `@MOD_VERSION` in our source files.
    id("dev.deftu.gradle.tools.minecraft.loom") // Applies the Loom plugin, which automagically configures Essential's Architectury Loom plugin for you.
    id("dev.deftu.gradle.tools.shadow") // Applies the Shadow plugin, which allows us to shade our dependencies into our mod JAR. This is NOT recommended for Fabric mods, but we have an *additional* configuration for those!
    id("dev.deftu.gradle.tools.minecraft.releases") // Applies the Minecraft auto-releasing plugin, which allows you to automatically release your mod to CurseForge and Modrinth.
}

toolkitLoomHelper {
    useOneConfig {
        version = "1.0.0-alpha.134"
        loaderVersion = "1.1.0-alpha.48"

        usePolyMixin = true
        polyMixinVersion = "0.8.4+build.6"

        applyLoaderTweaker = true

        for (module in arrayOf("commands", "config", "config-impl", "events", "internal", "hud", "ui", "utils")) {
            +module
        }
    }

    // Turns off the server-side run configs, as we're building a client-sided mod.
    disableRunConfigs(GameSide.SERVER)

    // Defines the name of the Mixin refmap, which is used to map the Mixin classes to the obfuscated Minecraft classes.
    if (!mcData.isNeoForge) {
        useMixinRefMap(modData.id)
    }

    if (mcData.isForge) {
        // Configures the Mixin tweaker if we are building for Forge.
        useForgeMixin(modData.id)
    }
}

repositories {
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
}

dependencies {
    implementation(includeOrShade("gs.mclo:api:3.0.1")!!)
    if (mcData.version >= MinecraftVersions.VERSION_1_16) {
        data class CompatDependency(
            val forge: String,
            val fabric: String,
            val neoforge: String
        )

        fun DependencyHandlerScope.modImplementationCompat(notation: CompatDependency?) {
            notation?.let {
                when {
                    mcData.isNeoForge -> modImplementation(it.neoforge)
                    mcData.isForge -> modImplementation(it.forge)
                    mcData.isFabric -> modImplementation(it.fabric)
                    else -> error("Unsupported loader type: ${mcData.loader}")
                }
            }
        }

        fun nec(mcVersion: String, modVersion: String) =
            mcVersion to CompatDependency(
                fabric = "maven.modrinth:notenoughcrashes:$modVersion+$mcVersion-fabric",
                forge = "maven.modrinth:notenoughcrashes:$modVersion+$mcVersion-forge",
                neoforge = "maven.modrinth:notenoughcrashes:$modVersion+$mcVersion-neoforge"
            )

        val nec = mapOf(
            nec("1.16.5", "4.1.4"),
            nec("1.17.1", "4.1.4"),
            nec("1.18.2", "4.2.0"),
            nec("1.19.2", "5.0.0"),
            nec("1.19.4", "4.4.1"),
            nec("1.20.1", "4.4.9"),
            nec("1.20.4", "4.4.7"),
            nec("1.20.6", "4.4.7"),
            nec("1.21.1", "4.4.9"),
            nec("1.21.4", "4.4.8"),
            nec("1.21.5", "4.4.9")
        )

        modImplementationCompat(nec[mcData.version.toString()])
    }
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}