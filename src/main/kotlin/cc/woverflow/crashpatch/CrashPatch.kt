package cc.woverflow.crashpatch

import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.DeobfuscatingRewritePolicy
import cc.woverflow.crashpatch.hooks.ModsCheckerPlugin
import cc.woverflow.onecore.utils.Updater
import cc.woverflow.onecore.utils.command
import cc.woverflow.onecore.utils.sendBrandedNotification
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import gg.essential.universal.ChatColor
import gg.essential.universal.UDesktop
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.settings.GameSettings
import net.minecraft.launchwrapper.Launch
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.io.File
import java.lang.reflect.Field


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
    var stopChecking = false

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
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onGuiDraw(event: GuiScreenEvent.DrawScreenEvent.Post) {
        if (!stopChecking && event.gui !is GuiMainMenu) {
            stopChecking = true
            try {
                val settingsClass: Class<GameSettings> = Minecraft.getMinecraft().gameSettings.javaClass
                val field = settingsClass.getFieldAndSetAccessible("ofConnectedTextures")
                field?.let { property ->
                    try {
                        property.getInt(Minecraft.getMinecraft().gameSettings).let {
                            if (it == 0 || it == 3) {
                                sendBrandedNotification("CrashPatch", "CrashPatch fixes the Connected Textures crash on Forge.\n\nClick here to enable Connected Textures!", duration = 10f, action = {
                                    var reload = true
                                    try {
                                        property.setInt(Minecraft.getMinecraft().gameSettings, 2)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        reload = false
                                    }
                                    if (reload) {
                                        Minecraft.getMinecraft().gameSettings.saveOptions()
                                        Minecraft.getMinecraft().gameSettings.loadOptions()
                                        Minecraft.getMinecraft().refreshResources()
                                    }
                                })
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun Class<*>.getFieldAndSetAccessible(name: String): Field? {
        return try {
            val field = this.getDeclaredField(name)
            field.isAccessible = true
            field
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
val logger = LogManager.getLogger(CrashPatch)