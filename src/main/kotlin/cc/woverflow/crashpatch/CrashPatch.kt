package cc.woverflow.crashpatch

import cc.woverflow.wcore.utils.Updater
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import java.io.File

@Mod(modid = CrashPatch.MODID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object CrashPatch {
    lateinit var modDir: File
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    var useOldRepo = false
    private set

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        modDir = File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME)
        if (!modDir.exists()) modDir.mkdirs()
        Updater.addToUpdater(e.sourceFile, NAME, MODID, VERSION, "W-OVERFLOW/$MODID")
    }

    @Mod.EventHandler
    fun onPostInitialization(event: FMLPostInitializationEvent) {
        useOldRepo = File(modDir, "useoldrepo").exists()
    }
}
val logger = LogManager.getLogger()