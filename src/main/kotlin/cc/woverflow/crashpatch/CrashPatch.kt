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
    val modDir by lazy { File(File(Minecraft.getMinecraft().mcDataDir, "W-OVERFLOW"), NAME).also { if (!it.exists()) it.mkdirs() } }
    const val MODID = "crashpatch"
    const val NAME = "CrashPatch"
    const val VERSION = "@VERSION@"
    val useOldRepo by lazy { File(modDir, "useoldrepo").exists() }
    val isSkyclient by lazy { File(modDir, "SKYCLIENT").exists() }

    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        Updater.addToUpdater(e.sourceFile, NAME, MODID, VERSION, "W-OVERFLOW/$MODID")
    }

    @Mod.EventHandler
    fun onPost(e: FMLPostInitializationEvent) {
        throw NullPointerException()
    }
}
val logger = LogManager.getLogger()