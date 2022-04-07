package cc.woverflow.crashpatch

import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.DeobfuscatingRewritePolicy
import cc.woverflow.onecore.utils.Updater
import cc.woverflow.onecore.utils.asJsonElement
import cc.woverflow.onecore.utils.command
import com.google.gson.stream.MalformedJsonException
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import gg.essential.universal.ChatColor
import gg.essential.universal.UDesktop
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
                                String(availableBytes).asJsonElement().asJsonArray[0].asJsonObject
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
    val gameDir: File by lazy(LazyThreadSafetyMode.PUBLICATION) {
        try {
            if (Launch.minecraftHome.parentFile?.name == (if (UDesktop.isMac) "minecraft" else ".minecraft")) Launch.minecraftHome.parentFile else Launch.minecraftHome
        } catch (e: Exception) {
            e.printStackTrace()
            Launch.minecraftHome
        }
    }

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        DeobfuscatingRewritePolicy.install()
        Multithreading.runAsync {
            logger.info("Is SkyClient: $isSkyclient")
            if (!CrashHelper.loadJson()) {
                logger.warn("CrashHelper failed to preload crash data JSON!")
            }
        }
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
}
val logger = LogManager.getLogger(CrashPatch)