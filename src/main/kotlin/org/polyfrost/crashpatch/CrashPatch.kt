package org.polyfrost.crashpatch

//#if FORGE
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
//#else
//$$ import net.fabricmc.api.ClientModInitializer
//#endif

import java.io.File
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.crashpatch.crashes.DeobfuscatingRewritePolicy
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.utils.v1.Multithreading

//#if FORGE
@Mod(modid = CrashPatch.ID, version = CrashPatch.VERSION, name = CrashPatch.NAME, modLanguageAdapter = "org.polyfrost.oneconfig.utils.v1.forge.KotlinLanguageAdapter")
//#endif
object CrashPatch
    //#if FABRIC
    //$$ : ClientModInitializer
    //#endif
{

    const val ID = "@MOD_ID@"
    const val NAME = "@MOD_NAME@"
    const val VERSION = "@MOD_VERSION@"

    private val logger = LogManager.getLogger()

    @JvmStatic
    val mcDir by lazy(LazyThreadSafetyMode.PUBLICATION) {
        File(System.getProperty("user.dir"))
    }

    @JvmStatic
    val gameDir by lazy(LazyThreadSafetyMode.PUBLICATION) {
        try {
            if (mcDir.parentFile?.name?.let { name ->
                name == ".minecraft" || name == "minecraft"
            } == true) mcDir.parentFile else mcDir
        } catch (e: Exception) {
            e.printStackTrace()
            mcDir
        }
    }

    val isSkyclient by lazy(LazyThreadSafetyMode.PUBLICATION) {
        File(mcDir, "OneConfig/CrashPatch/SKYCLIENT").exists() || File(mcDir, "W-OVERFLOW/CrashPatch/SKYCLIENT").exists()
    }

    var requestedCrash = false

    fun preInitialize() {
        DeobfuscatingRewritePolicy.install()
        Multithreading.submit {
            logger.info("Is SkyClient: $isSkyclient")
            if (!CrashScanStorage.downloadJson()) {
                logger.error("CrashHelper failed to preload crash data JSON!")
            }
        }
    }

    fun initialize() {
        CommandManager.registerCommand(CrashPatchCommand)
        CrashPatchConfig // Initialize the config
    }

    //#if FORGE
    @Mod.EventHandler
    fun onPreInit(e: FMLPreInitializationEvent) {
        preInitialize()
    }

    @Mod.EventHandler
    fun onInit(e: FMLInitializationEvent) {
        initialize()
    }
    //#else
    //$$ override fun onInitializeClient() {
    //$$     preInitialize()
    //$$     initialize()
    //$$ }
    //#endif

}
