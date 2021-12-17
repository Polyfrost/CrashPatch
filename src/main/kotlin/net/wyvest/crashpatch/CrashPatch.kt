package net.wyvest.crashpatch

import net.minecraft.client.Minecraft
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.wyvest.crashpatch.utils.Updater
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    lateinit var jarFile: File
    lateinit var modDir: File
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val logger: Logger = LogManager.getLogger(NAME)

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)
        if (!modDir.exists()) modDir.mkdirs()
        jarFile = e.sourceFile
    }
    @Mod.EventHandler
    fun onPostInitialization(event: FMLPostInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
        Updater.update()
    }
}