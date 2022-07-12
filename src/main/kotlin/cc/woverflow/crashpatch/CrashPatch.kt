package cc.woverflow.crashpatch

import cc.polyfrost.oneconfig.libs.universal.ChatColor
import cc.polyfrost.oneconfig.libs.universal.UDesktop
import cc.polyfrost.oneconfig.libs.universal.UMinecraft
import cc.polyfrost.oneconfig.utils.Multithreading
import cc.polyfrost.oneconfig.utils.commands.CommandManager
import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.DeobfuscatingRewritePolicy
import cc.woverflow.crashpatch.hooks.ModsCheckerPlugin
import net.minecraft.launchwrapper.Launch
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File


@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    val modDir by lazy { File(File(Launch.minecraftHome, "W-OVERFLOW"), NAME).also { if (!it.exists()) it.mkdirs() } }
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) { File(modDir, "SKYCLIENT").exists() || ModsCheckerPlugin.modsMap.keys.any { it == "skyclientcosmetics" || it == "scc" || it == "skyclientaddons" || it == "skyblockclientupdater" || it == "skyclientupdater" || it == "skyclientcore" } }
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
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        CommandManager.INSTANCE.registerCommand(CrashPatchCommand.Companion::class.java)
    }

    @Command(value = "reloadcrashpatch")
    class CrashPatchCommand {
        companion object {
            @Main
            fun main() {
                if (CrashHelper.loadJson()) {
                    UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Successfully reloaded JSON file!"))
                } else {
                    UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Failed to reload the JSON file!"))
                }
            }
        }
    }
}
val logger: Logger = LogManager.getLogger(CrashPatch)