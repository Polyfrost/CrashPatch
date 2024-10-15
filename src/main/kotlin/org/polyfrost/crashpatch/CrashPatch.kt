package org.polyfrost.crashpatch

import org.polyfrost.universal.ChatColor
import org.polyfrost.universal.UMinecraft
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command
import org.polyfrost.crashpatch.config.CrashPatchConfig
import org.polyfrost.crashpatch.crashes.CrashHelper
import org.polyfrost.crashpatch.crashes.DeobfuscatingRewritePolicy
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.polyfrost.utils.v1.dsl.openUI
import java.io.File


@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter")
object CrashPatch {
    const val MODID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"
    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) { File(mcDir, "OneConfig/CrashPatch/SKYCLIENT").exists() || File(
        mcDir, "W-OVERFLOW/CrashPatch/SKYCLIENT").exists() }

    var test = false

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        DeobfuscatingRewritePolicy.install()
        Multithreading.submit {
            logger.info("Is SkyClient: $isSkyclient")
            if (!CrashHelper.loadJson()) {
                logger.error("CrashHelper failed to preload crash data JSON!")
            }
        }
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        CommandManager.registerCommand(CrashPatchCommand())
        CrashPatchConfig
        // uncomment to test init screen crashes
//        throw Throwable("java.lang.NoClassDefFoundError: xyz/matthewtgm/requisite/keybinds/KeyBind at lumien.custommainmenu.configuration.ConfigurationLoader.load(ConfigurationLoader.java:142) club.sk1er.bossbarcustomizer.BossbarMod.loadConfig cc.woverflow.hytils.handlers.chat.modules.modifiers.DefaultChatRestyler Failed to login: null The Hypixel Alpha server is currently closed! net.kdt.pojavlaunch macromodmodules")
    }

    @Command("crashpatch")
    class CrashPatchCommand {
        @Command
        fun main() {
            CrashPatchConfig.openUI()
        }

        @Command
        fun reload() {
            if (CrashHelper.loadJson()) {
                CrashHelper.simpleCache.clear()
                UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Successfully reloaded JSON file!"))
            } else {
                UMinecraft.getMinecraft().thePlayer.addChatMessage(ChatComponentText("${ChatColor.RED}[CrashPatch] Failed to reload the JSON file!"))
            }
        }

        @Command
        fun crash() {
            test = true
        }
    }
}
val logger: Logger = LogManager.getLogger(CrashPatch)
val gameDir: File by lazy(LazyThreadSafetyMode.PUBLICATION) {
    val file = mcDir
    try {
        if (file.parentFile?.name?.let { it == ".minecraft" || it == "minecraft" } == true) file.parentFile else file
    } catch (e: Exception) {
        e.printStackTrace()
        file
    }
}
val mcDir = File(System.getProperty("user.dir"))
val mc
    get() = UMinecraft.getMinecraft() // todo replace with oneconfig in alpha20