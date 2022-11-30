package cc.woverflow.crashpatch

import cc.polyfrost.oneconfig.libs.universal.ChatColor
import cc.polyfrost.oneconfig.libs.universal.UDesktop
import cc.polyfrost.oneconfig.libs.universal.UMinecraft
import cc.polyfrost.oneconfig.utils.Multithreading
import cc.polyfrost.oneconfig.utils.commands.CommandManager
import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand
import cc.polyfrost.oneconfig.utils.dsl.tick
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


@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "cc.polyfrost.oneconfig.utils.KotlinLanguageAdapter")
object CrashPatch {
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) { File("./OneConfig/CrashPatch/SKYCLIENT").exists() || File("./W-OVERFLOW/CrashPatch/SKYCLIENT").exists() || ModsCheckerPlugin.modsMap.keys.any { it == "skyclientcosmetics" || it == "scc" || it == "skyclientaddons" || it == "skyblockclientupdater" || it == "skyclientupdater" || it == "skyclientcore" } }
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
                logger.error("CrashHelper failed to preload crash data JSON!")
            }
        }
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        CommandManager.INSTANCE.registerCommand(CrashPatchCommand())
    }

    @Command(value = "reloadcrashpatch")
    class CrashPatchCommand {
        @Main
        fun main() {
            if (CrashHelper.loadJson()) {
                UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Successfully reloaded JSON file!"))
            } else {
                UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Failed to reload the JSON file!"))
            }
        }

        var a = false //todo we should probably make crashpatch like make things stop thorwing recuresivley or whatever

        @SubCommand
        fun crash() {
            UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Crashing..."))
            tick(1) {
                if (!a) {
                    a = true
                    throw Throwable("CrashPatch test crash")

                }
            }
        }
    }
}
val logger: Logger = LogManager.getLogger(CrashPatch)