package org.polyfrost.crashpatch.client

import dev.deftu.textile.minecraft.MCSimpleTextHolder
import dev.deftu.textile.minecraft.MCTextFormat
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConfig
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.utils.v1.dsl.openUI
import java.io.File

object CrashPatchClient {

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
        //#if MC<1.13
        org.polyfrost.crashpatch.crashes.DeobfuscatingRewritePolicy.install()
        //#endif
        Multithreading.submit {
            logger.info("Is SkyClient: $isSkyclient")
            if (!CrashScanStorage.downloadJson()) {
                logger.error("CrashHelper failed to preload crash data JSON!")
            }
        }

        //#if MC<1.13
        if (System.getProperty("polyfrost.crashpatch.init_crash") == "true") {
            throw RuntimeException("Crash requested by CrashPatch")
        }
        //#endif
    }

    fun initialize() {
        CommandManager.register(with(CommandManager.literal(CrashPatchConstants.ID)) {
            executes {
                CrashPatchConfig.openUI()
                1
            }

            then(CommandManager.literal("reload").executes { ctx ->
                val text = if (CrashScanStorage.downloadJson()) {
                    MCSimpleTextHolder("[${CrashPatchConstants.NAME}] Successfully reloaded JSON file!").withFormatting(MCTextFormat.Companion.GREEN)
                } else {
                    MCSimpleTextHolder("[${CrashPatchConstants.NAME}] Failed to reload JSON file!").withFormatting(MCTextFormat.Companion.RED)
                }

                ctx.source.replyChat(text)
            })

            then(CommandManager.literal("crash").executes { ctx ->
                requestedCrash = true
                1
            })
        })

        CrashPatchConfig // Initialize the config
    }

}