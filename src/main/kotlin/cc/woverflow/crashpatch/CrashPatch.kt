package cc.woverflow.crashpatch

import cc.woverflow.wcore.utils.Updater
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.zip.ZipFile


@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    val modDir by lazy { File(File(Launch.minecraftHome, "W-OVERFLOW"), NAME).also { if (!it.exists()) it.mkdirs() } }
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val useOldRepo by lazy(LazyThreadSafetyMode.PUBLICATION) { File(modDir, "useoldrepo").exists() }
    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) { File(modDir, "SKYCLIENT").exists() || File(Launch.minecraftHome, "mods").listFiles { _, name -> name.endsWith(".jar") }?.let { list ->
        list.forEach {
            try {
                ZipFile(it).use { zipFile ->
                    val entry = zipFile.getEntry("mcmod.info")
                    if (entry != null) {
                        zipFile.getInputStream(entry).use { inputStream ->
                            val availableBytes = ByteArray(inputStream.available())
                            inputStream.read(availableBytes, 0, inputStream.available())
                            val modInfo =
                                parser.parse(String(availableBytes)).asJsonArray[0].asJsonObject
                            if (!modInfo.has("modid")) {
                                return@forEach
                            }
                            val modid = modInfo["modid"].asString
                            if (modid == "skyclientcosmetics" || modid == "scc" || modid == "skyclientaddons" || modid == "skyblockclientupdater") {
                                return@let true
                            }
                        }
                    }
                }
            } catch (ignored: MalformedJsonException) {
            } catch (ignored: IllegalStateException) {
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return@let false
    } ?: false }

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        Updater.addToUpdater(e.sourceFile, NAME, MODID, VERSION, "W-OVERFLOW/$MODID")
    }
}
val logger = LogManager.getLogger(CrashPatch)
val parser = JsonParser()