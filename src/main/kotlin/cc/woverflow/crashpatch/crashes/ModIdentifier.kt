package cc.woverflow.crashpatch.crashes

import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
import org.apache.logging.log4j.LogManager
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.net.URL

object ModIdentifier {
    private val log = LogManager.getLogger()

    fun identifyFromStacktrace(e: Throwable?): ModContainer? {
        var theThrowable = e
        val modMap = makeModMap()

        // Get the set of classes
        val classes = LinkedHashSet<String>()
        while (theThrowable != null) {
            for (element in theThrowable.stackTrace) {
                classes.add(element.className)
            }
            theThrowable = theThrowable.cause
        }
        val mods = LinkedHashSet<ModContainer>()
        for (className in classes) {
            val classMods = identifyFromClass(className, modMap)
            mods.addAll(classMods)
        }
        return mods.firstOrNull()
    }

    private fun identifyFromClass(className: String, modMap: Map<File, MutableSet<ModContainer>>): Set<ModContainer> {
        // Skip identification for Mixin, one's mod copy of the library is shared with all other mods
        if (className.startsWith("org.spongepowered.asm.mixin.")) return emptySet()

        // Get the URL of the class
        val untrasformedName = untransformName(Launch.classLoader, className)
        var url = Launch.classLoader.getResource(untrasformedName.replace('.', '/') + ".class")
        log.debug("$className = $untrasformedName = $url")
        if (url == null) {
            log.warn("Failed to identify $className (untransformed name: $untrasformedName)")
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
