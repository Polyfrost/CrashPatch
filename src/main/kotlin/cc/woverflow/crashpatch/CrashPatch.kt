package cc.woverflow.crashpatch

import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import cc.woverflow.crashpatch.utils.Updater
import gg.essential.api.EssentialAPI
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.io.File

@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    lateinit var jarFile: File
    lateinit var modDir: File
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"

    private val devEnv by lazy { EssentialAPI.getMinecraftUtil().isDevelopment() }
    private val keyBinding = KeyBinding("Crash", Keyboard.KEY_NONE, "Crash Patch")

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = e.sourceFile
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        if (devEnv) {
            ClientRegistry.registerKeyBinding(keyBinding)
        }
    }

    @Mod.EventHandler
    fun onPostInitialization(event: FMLPostInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
        Updater.update()
    }

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if (devEnv) {
            if (keyBinding.isPressed) {
                throw NullPointerException("java.lang.IllegalArgumentException: Cannot get property PropertyEnum Failed to login: null at club.sk1er.patcher.registry.AsyncBlockAndItems.load(AsyncBlockAndItems.kt:28) BetterFPS: true")
            }
        }
    }
}