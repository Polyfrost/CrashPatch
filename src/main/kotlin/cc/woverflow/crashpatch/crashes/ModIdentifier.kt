/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/client/MixinMinecraft.java
 *The source file uses the MIT License.
 */

package cc.woverflow.crashpatch.crashes

import cc.woverflow.crashpatch.logger
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

object ModIdentifier {

    fun identifyFromStacktrace(e: Throwable?): ModContainer? {
        val modMap = makeModMap()

        // Get the set of classes
        val classes = LinkedHashSet<String>()
        e?.stackTrace?.forEachIndexed { index, stackTraceElement ->
            if (index < 4) { // everything after the first 3 lines are basically useless and only leads to false detections
                classes.add(stackTraceElement.className)
            }
        }
        val mods = LinkedHashSet<ModContainer>()
        for (className in classes) {
            val classMods = identifyFromClass(className, modMap)
            if (classMods.isNotEmpty()) {
                mods.addAll(classMods)
            }
        }
        return mods.firstOrNull()
    }

    private fun identifyFromClass(className: String, modMap: Map<File, MutableSet<ModContainer>>): Set<ModContainer> {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) return emptySet()

        // Get the URL of the class
        val untrasformedName = untransformName(Launch.classLoader, className)
        var url = Launch.classLoader.getResource(untrasformedName.replace('.', '/') + ".class")
        logger.debug("$className = $untrasformedName = $url")
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
    }

    private fun makeModMap(): Map<File, MutableSet<ModContainer>> {
        val modMap = HashMap<File, MutableSet<ModContainer>>()
        for (mod in Loader.instance().modList) {
            val currentMods = modMap.getOrDefault(mod.source, HashSet())
            currentMods.add(mod)
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
        return modMap
    }

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
}
