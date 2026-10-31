import dev.detekt.gradle.Detekt
import net.ltgt.gradle.errorprone.errorprone
import net.ornithemc.ploceus.api.PloceusGradleExtensionApi
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("dev.kikugie.loom-back-compat")
    id("org.jetbrains.kotlin.jvm") version "2.4.20"
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("ploceus") version "1.17.4" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20"
    id("org.jetbrains.compose") version "1.11.1"
    id("dev.deftu.gradle.bloom") version "0.2.0"
    id("me.modmuss50.mod-publish-plugin") version "2.2.0"
    id("dev.detekt") version "2.0.0-alpha.6"
    id("net.ltgt.errorprone") version "5.1.1"
}

val isOrnithe = stonecutter.current.version == "1.8.9"
val ploceus = if (isOrnithe) {
    pluginManager.apply("net.fabricmc.fabric-loom-remap")
    pluginManager.apply("ploceus")

    configurations.configureEach {
        exclude(group = "org.lwjgl.lwjgl")
    }

    extensions.getByType<PloceusGradleExtensionApi>().apply {
        setIntermediaryGeneration(2)
    }
} else {
    null
}

val modid: String = sc.properties["mod.id"]
val modname: String = sc.properties["mod.name"]
val modversion: String = sc.properties["mod.version"]
val mcversion: String = sc.current.version
val mcDependencyVersion: String = sc.properties.getOrNull<String>("deps.minecraft") ?: mcversion
val mcline: String = sc.current.project
val versionrange: String = sc.properties["mod.mc_compat"]
val loaderversion: String = sc.properties["deps.fabric_loader"]
val oneconfigversion: String = sc.properties["deps.oneconfig"]
val loader = if (isOrnithe) "ornithe" else "fabric"

version = "$modversion+$mcversion"
base.archivesName = modname

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_25
}

val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

repositories {
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }

    mavenLocal()
    mavenCentral()
    google()
    maven("https://repo.polyfrost.org/releases") { name = "Polyfrost Releases" }
    maven("https://repo.polyfrost.org/snapshots") { name = "Polyfrost Snapshots" }
    maven("https://redirector.kotlinlang.org/maven/compose-dev") { name = "Compose Dev" }
    maven("https://central.sonatype.com/repository/maven-snapshots") {
        name = "Sonatype Snapshots"
        content { includeGroup("net.kyori") }
    }
    maven("https://maven.cloverclient.com/releases") {
        content { includeGroup("pl.tomgirl") }
    }
    maven("https://maven.bawnorton.com/releases") {
        name = "Bawnorton"
        content { includeGroupAndSubgroups("com.github.bawnorton") }
    }
    strictMaven("https://maven.deftu.dev/releases", "Deftu", "dev.deftu")
    strictMaven("https://maven.terraformersmc.com/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.fabricmc.net/", "FabricMC", "net.fabricmc")
    strictMaven("https://www.cursemaven.com", "CurseForge", "curse.maven")
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcDependencyVersion")
    val datafixerupper = "com.mojang:datafixerupper:${sc.properties.get<String>("deps.datafixerupper")}"
    if (isOrnithe) {
        include(implementation(datafixerupper) { isTransitive = false })
        // ploceus is always applied on Ornithe
        mappings(ploceus!!.layeredMappings {
            mappings("net.ornithemc:feather-gen2:$mcversion+build.${sc.properties["feather_build"] as String}:v2") {
                containsUnpick()
            }
            mappings(rootProject.file("mappings/feather-overrides.tiny"))
        })
        testCompileOnly("net.ornithemc.osl-gen2:entrypoints:${sc.properties["deps.osl_entrypoints"] as String}")
    } else {
        compileOnly(datafixerupper)
        loomx.applyMojangMappings()
    }

    modImplementation("net.fabricmc:fabric-loader:$loaderversion")
    if (!isOrnithe) {
        val fapiversion: String = sc.properties["deps.fabric_api"]
        val necversion: String = sc.properties["deps.nec"]
        modImplementation("net.fabricmc.fabric-api:fabric-api:$fapiversion")
        modImplementation("maven.modrinth:notenoughcrashes:$necversion-fabric")
    }

    val mixinsquared = sc.properties.get<String>("deps.mixinsquared")
    implementation(annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:$mixinsquared")!!)
    include(implementation("gs.mclo:api:${sc.properties.get<String>("deps.mclo")}")!!)

    modImplementation("org.polyfrost.oneconfig:$mcline-$loader:$oneconfigversion")
    for (module in arrayOf("config", "config-impl", "internal")) {
        implementation("org.polyfrost.oneconfig:$module:$oneconfigversion")
    }

    testImplementation("org.junit.jupiter:junit-jupiter:${sc.properties.get<String>("deps.junit")}")
    testImplementation("net.fabricmc:fabric-loader-junit:$loaderversion")

    errorprone("com.google.errorprone:error_prone_core:${sc.properties.get<String>("deps.errorprone")}")
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        disableAllChecks = true
        error("UnnecessarilyFullyQualified")
    }
}

// the enabled Detekt rules need type resolution, which only the per-compilation tasks have
tasks.named<Detekt>("detekt") {
    enabled = false
    dependsOn(tasks.withType<Detekt>().matching { it.name != "detekt" && !it.name.endsWith("SourceSet") })
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        //property("polyfrost.crashpatch.init_crash", "true")
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
        jvmArguments.add("-Dmixin.debug.export=true")
    }

    runConfigs.remove(runConfigs["server"])
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

val kotlinJvmTarget = JvmTarget.fromTarget(requiredJava.majorVersion)

tasks.withType<JavaCompile>().configureEach {
    options.release = requiredJava.majorVersion.toInt()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget = kotlinJvmTarget
    finalizedBy("detekt")
}

bloom {
    replacement("@MOD_ID@", modid)
    replacement("@MOD_NAME@", modname)
    replacement("@MOD_VERSION@", modversion)
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    processResources {
        val props = mapOf(
            "mod_id" to modid,
            "mod_name" to modname,
            "mod_version" to modversion,
            "minecraft_version_range" to versionrange,
            "loader_version" to loaderversion,
            "oneconfig_version" to oneconfigversion,
            "nec_version" to sc.properties.getOrNull<String>("deps.nec").orEmpty().substringBefore('+'),
        )

        inputs.properties(props)

        filesMatching("fabric.mod.json") {
            expand(props)
            if (isOrnithe) filter { line -> line.takeUnless { "notenoughcrashes" in it } }
        }
    }

    jar {
        inputs.property("archivesName", base.archivesName)

        from(rootProject.file("LICENSE")) {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", modversion)
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/$modversion"))
    }
}

val modrinthId = listOf("oneconfig.publish.modrinth", "publish.modrinth")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val modrinthToken = listOf("oneconfig.publish.modrinth.token", "publish.modrinth.token", "modrinth.token")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val curseforgeId = listOf("oneconfig.publish.curseforge", "publish.curseforge")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }
val curseforgeToken = listOf("oneconfig.publish.curseforge.token", "publish.curseforge.token", "curseforge.token")
    .firstNotNullOfOrNull { findProperty(it) }?.toString()?.takeIf { it.isNotBlank() }

val changelogs = rootProject.file("CHANGELOG.md").takeIf { it.exists() }?.readText() ?: "No changelog provided."

val validateChangelog = tasks.register("validateChangelog") {
    description = "Validates that the changelog is written for the current version."
    doLast {
        if (!changelogs.contains(modversion)) {
            throw GradleException("Changelog for version $modversion not found.")
        }
    }
}

tasks.publishMods.configure {
    dependsOn(validateChangelog)
}
tasks.matching { it.name == "publishModrinth" || it.name == "publishCurseforge" }.configureEach {
    dependsOn(validateChangelog)
}

publishMods {
    file = loomx.modJar.flatMap { it.archiveFile }

    displayName = modversion
    version = "v$modversion"
    changelog = changelogs
    type = STABLE

    modLoaders.add(loader)

    dryRun = modrinthId == null || modrinthToken == null || (curseforgeId != null && curseforgeToken == null)

    if (modrinthId != null) {
        modrinth {
            projectId = modrinthId
            accessToken = modrinthToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcversion) })

            requires("oneconfig")
            requires("fabric-language-kotlin")
            if (!isOrnithe) requires("notenoughcrashes")
        }
    }

    if (curseforgeId != null && !isOrnithe) {
        curseforge {
            projectId = curseforgeId
            accessToken = curseforgeToken.orEmpty()

            minecraftVersions.addAll(compatibleVersions.ifEmpty { listOf(mcversion) })
            client = true

            requires("oneconfig")
            requires("fabric-language-kotlin")
            requires("not-enough-crashes")
        }
    }
}

if (curseforgeId != null && isOrnithe) tasks.register("publishCurseforge")
