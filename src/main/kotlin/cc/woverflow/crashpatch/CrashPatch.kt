package cc.woverflow.crashpatch

import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.DeobfuscatingRewritePolicy
import cc.woverflow.crashpatch.hooks.ModsCheckerPlugin
import cc.woverflow.onecore.utils.Updater
import cc.woverflow.onecore.utils.command
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


@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    val modDir by lazy { File(File(Launch.minecraftHome, "W-OVERFLOW"), NAME).also { if (!it.exists()) it.mkdirs() } }
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) { File(modDir, "SKYCLIENT").exists() || ModsCheckerPlugin.modsMap.keys.any { it == "skyclientcosmetics" || it == "scc" || it == "skyclientaddons" || it == "skyblockclientupdater" } }
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