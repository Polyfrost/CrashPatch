package cc.woverflow.crashpatch

import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.wcore.utils.Updater
import cc.woverflow.wcore.utils.command
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import gg.essential.api.EssentialAPI
import gg.essential.universal.ChatColor
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
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
    var isSkyclient = false
    private set
    var isPatcher = false
    private set

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        Updater.addToUpdater(e.sourceFile, NAME, MODID, VERSION, "W-OVERFLOW/$MODID")
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        command("reloadcrashpatch", generateHelpCommand = false) {
            main {
                if (CrashHelper.loadJson()) {
                    EssentialAPI.getMinecraftUtil().sendMessage("${ChatColor.RED}[CrashPatch] ", "Successfully reloaded JSON file!")
                } else {
                    EssentialAPI.getMinecraftUtil().sendMessage("${ChatColor.RED}[CrashPatch] ", "Failed to reloaded JSON file!")
                }
            }
        }
    }

    fun findMods() {
        isSkyclient = File(modDir, "SKYCLIENT").exists()
        File(Launch.minecraftHome, "mods").listFiles { _, name -> name.endsWith(".jar") }?.let { list ->
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
                                when (modInfo["modid"].asString) {
                                    "skyclientcosmetics", "scc", "skyclientaddons", "skyblockclientupdater" -> isSkyclient = true
                                    "patcher" -> isPatcher = true
                                }
                                if (isPatcher && isSkyclient) {
                                    return@let
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
        }
    }
}
val logger = LogManager.getLogger(CrashPatch)
val parser = JsonParser()