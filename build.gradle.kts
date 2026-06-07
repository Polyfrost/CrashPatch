import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.0"
    id("dev.kikugie.loom-back-compat")
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("me.modmuss50.mod-publish-plugin") version "2.0.0"
}

val modid = property("mod.id")
val modname = property("mod.name")
val modversion = property("mod.version")
val mcversion = stonecutter.current.version
val versionoverride = property("minecraft_version")

val loaderversion = property("loader_version")
val oneconfigversion = property("oneconfig_version")

val necversion = property("nec_version")

version = "$modversion+$mcversion"
base.archivesName = modname.toString()

repositories {
    mavenCentral()
    maven("https://maven.parchmentmc.org")
    maven("https://repo.polyfrost.org/releases")
    maven("https://repo.polyfrost.org/snapshots")
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://maven.bawnorton.com/releases") {
        content { includeGroup("com.github.bawnorton.mixinsquared") }
    }
    maven("https://redirector.kotlinlang.org/maven/compose-dev")
    google()
}

loom {
    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }

    runConfigs.remove(runConfigs["server"])
}

dependencies {
    minecraft("com.mojang:minecraft:$mcversion")
    compileOnly("com.mojang:datafixerupper:4.0.26")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:$loaderversion")
    modImplementation("maven.modrinth:notenoughcrashes:$necversion+$mcversion-fabric")
    implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.3")!!)
    implementation("gs.mclo:api:3.0.1")

    modImplementation("org.polyfrost.oneconfig:$versionoverride-fabric:$oneconfigversion")
    for (module in arrayOf("config", "config-impl", "internal")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigversion")
    }
}

bloom {
    replacement("@MOD_ID@", modid!!)
    replacement("@MOD_NAME@", modname!!)
    replacement("@MOD_VERSION@", modversion!!)
}

tasks.processResources {
    val props = mapOf(
        "mod_id" to modid,
        "mod_name" to modname,
        "mod_version" to modversion,
        "mc_version" to mcversion,
        "loader_version" to providers.gradleProperty("loader_version").get()
    )

    inputs.properties(props)

    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.build {
    doLast {
        val sourceFile = rootProject.projectDir.resolve("versions/${project.name}/build/libs/crashpatch.jar")
        val targetFile = rootProject.projectDir.resolve("build/libs/crashpatch-${stonecutter.current.version}.jar")
        targetFile.parentFile.mkdirs()
        targetFile.writeBytes(sourceFile.readBytes())
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(25)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_25)
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
    inputs.property("archivesName", base.archivesName)

    from("LICENSE") {
        rename { "${it}_${inputs.properties["archivesName"]}" }
    }
}

fun <T> optionalProp(property: String, block: (String) -> T?): T? =
    findProperty(property)?.toString()?.takeUnless { it.isBlank() }?.let(block)

val modrinthId = findProperty("publish.modrinth")?.toString()?.takeIf { it.isNotBlank() }

// make sure modrinth.token is set in your user gradle properties
publishMods {
    val jarTask = if (stonecutter.eval(stonecutter.current.version, ">=26.1")) {
        project.tasks.named<AbstractArchiveTask>("jar")
    } else {
        project.tasks.named<AbstractArchiveTask>("remapJar")
    }.get()

    file.set(jarTask.archiveFile)

    displayName = modversion.toString()
    version = "v$modversion"
    changelog.set(project.rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided.")
    type.set(ALPHA)

    modLoaders.add("fabric")

    dryRun = modrinthId == null

    if (modrinthId != null) {
        modrinth {
            projectId.set(property("publish.modrinth").toString())
            accessToken.set(findProperty("modrinth.token").toString())

            minecraftVersions.add(mcversion)

            requires("oneconfig")
        }
    }
}