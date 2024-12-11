package org.polyfrost.crashpatch.identifier

//#if FORGE
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.common.Loader
//#else
//$$ import net.fabricmc.loader.api.FabricLoader
//$$ import org.jetbrains.annotations.Nullable
//$$ import org.spongepowered.asm.mixin.extensibility.IMixinInfo
//$$ import org.spongepowered.asm.mixin.transformer.ClassInfo
//$$ import java.lang.reflect.Field
//$$ import java.nio.file.Path
//$$ import java.nio.file.Paths
//#endif

import java.io.File
import org.apache.logging.log4j.LogManager

typealias ModMap = Map<File, MutableSet<ModMetadata>>

object ModIdentifier {

    private val logger = LogManager.getLogger()

    fun identifyFromStacktrace(e: Throwable?): ModMetadata? {
        val modMap = makeModMap()

        // Get the set of classes
        val classes = LinkedHashSet<String>()
        e?.stackTrace?.forEachIndexed { index, stackTraceElement ->
            if (index < 4) { // everything after the first 3 lines are basically useless and only leads to false detections
                classes.add(stackTraceElement.className)
            }
        }

        val mods = LinkedHashSet<ModMetadata>()
        for (className in classes) {
            val classMods = identifyFromClass(className, modMap)
            if (classMods.isNotEmpty()) {
                mods.addAll(classMods)
            }
        }

        return mods.firstOrNull()
    }

    private fun identifyFromClass(className: String, modMap: ModMap): Set<ModMetadata> {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) {
            return emptySet()
        }

        //#if FORGE
        // Get the URL of the class
        val untrasformedName = untransformName(Launch.classLoader, className)
        var url = Launch.classLoader.getResource(untrasformedName.replace('.', '/') + ".class")
        logger.debug("{} = {} = {}", className, untrasformedName, url)
        if (url == null) {
            logger.warn("Failed to identify $className (untransformed name: $untrasformedName)")
            return emptySet()
        }

        // Get the mod containing that class
        return try {
            if (url.protocol == "jar") url = URL(url.file.substring(0, url.file.indexOf('!')))
            modMap[File(url.toURI()).canonicalFile] ?: emptySet()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        //#else
        //$$ try {
        //$$     val clz = Class.forName(className)
        //$$     val codeSource = clz.protectionDomain.codeSource
        //$$     if (codeSource == null) {
        //$$         logger.debug("Failed to identify $className because of a null code source")
        //$$         return emptySet()
        //$$     }
        //$$
        //$$     val url = codeSource.location
        //$$     if (url == null) {
        //$$         logger.debug("Failed to identify $className because of a null URL")
        //$$         return emptySet()
        //$$     }
        //$$
        //$$     return getModsAt(Paths.get(url.toURI()), modMap)
        //$$ } catch (e: Exception) {
        //$$     logger.debug("Ignoring class $className for identification because of an error", e)
        //$$     return emptySet()
        //$$ }
        //#endif
    }

    //#if FABRIC
    //$$ private fun getModsAt(path: Path, modMap: ModMap): MutableSet<ModMetadata> {
    //$$     val mod: MutableSet<ModMetadata>? = modMap[path.toFile()]
    //$$     if (mod != null) return mod
    //$$
    //$$     else if (FabricLoader.getInstance().isDevelopmentEnvironment) {
    //$$         // For some reason, in dev, the mod being tested has the 'resources' folder as the origin instead of the 'classes' folder.
    //$$
    //$$         val resourcesPathString: String =
    //$$             path.toString().replace("\\", "/") // Make it work with Architectury as well
    //$$                 .replace("common/build/classes/java/main", "fabric/build/resources/main")
    //$$                 .replace("common/build/classes/kotlin/main", "fabric/build/resources/main")
    //$$                 .replace("classes/java/main", "resources/main")
    //$$                 .replace("classes/kotlin/main", "resources/main")
    //$$         val resourcesPath: Path = Paths.get(resourcesPathString)
    //$$         return modMap.getOrElse(resourcesPath.toFile()) { emptySet() }.toMutableSet()
    //$$     } else {
    //$$         logger.debug("Mod at path '" + path.toAbsolutePath() + "' is at fault, but it could not be found in the map of mod paths: ")
    //$$         return mutableSetOf()
    //$$     }
    //$$ }
    //#endif

    private fun makeModMap(): ModMap {
        val modMap = HashMap<File, MutableSet<ModMetadata>>()

        //#if FORGE
        for (mod in Loader.instance().modList) {
            val currentMods = modMap.getOrDefault(mod.source, HashSet())
            currentMods.add(ModMetadata(mod.modId, mod.name))

            try {
                modMap[mod.source.canonicalFile] = currentMods
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        try {
            modMap.remove(Loader.instance().minecraftModContainer.source) // Ignore minecraft jar (minecraft)
            modMap.remove(Loader.instance().indexedModList["FML"]!!.source) // Ignore forge jar (FML, forge)
        } catch (ignored: NullPointerException) {
            // Workaround for https://github.com/MinecraftForge/MinecraftForge/issues/4919
        }
        //#else
        //$$ for (modContainer in FabricLoader.getInstance().allMods) {
        //$$     val modMetadata = ModMetadata(modContainer.metadata.id, modContainer.metadata.name)
        //$$     for (source in modContainer.origin.paths) {
        //$$         modMap.computeIfAbsent(source.toFile()) { mutableSetOf() }.add(modMetadata)
        //$$     }
        //$$ }
        //#endif

        return modMap
    }

    //#if FORGE
    private fun untransformName(launchClassLoader: LaunchClassLoader, className: String): String {
        return try {
            val untransformNameMethod =
                LaunchClassLoader::class.java.getDeclaredMethod("untransformName", String::class.java)
            untransformNameMethod.isAccessible = true
            untransformNameMethod.invoke(launchClassLoader, className) as String
        } catch (e: ReflectiveOperationException) {
            throw RuntimeException(e)
        }
    }
    //#endif

    //#if FABRIC
    //$$ private object Reflection {
    //$$     var classInfoMixin: Field? = null
    //$$
    //$$     init {
    //$$         try {
    //$$             classInfoMixin = ClassInfo::class.java.getDeclaredField("mixin")
    //$$             classInfoMixin!!.isAccessible = true
    //$$         } catch (e: NoSuchFieldException) {
    //$$             throw java.lang.RuntimeException(e)
    //$$         }
    //$$     }
    //$$
    //$$     @Nullable
    //$$     fun getMixinInfo(classInfo: ClassInfo?): IMixinInfo {
    //$$         try {
    //$$             return classInfoMixin!!.get(classInfo) as IMixinInfo
    //$$         } catch (e: IllegalAccessException) {
    //$$             throw java.lang.RuntimeException(e)
    //$$         }
    //$$     }
    //$$ }
    //#endif

}
