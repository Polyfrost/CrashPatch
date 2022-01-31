package cc.woverflow.crashpatch

import cc.woverflow.wcore.utils.Updater
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    lateinit var jarFile: File
    lateinit var modDir: File
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)
        if (!modDir.exists()) modDir.mkdirs()
        Updater.addToUpdater(e.sourceFile)
    }

    @Mod.EventHandler
    fun onPostInitialization(event: FMLPostInitializationEvent) {

    }
}