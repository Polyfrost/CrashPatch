package org.polyfrost.crashpatch.client

import com.mojang.brigadier.Command
import dev.deftu.omnicore.api.client.commands.OmniClientCommands
import dev.deftu.omnicore.api.client.commands.command
import dev.deftu.textile.Text
import dev.deftu.textile.minecraft.MCTextStyle
import dev.deftu.textile.minecraft.TextColors
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConfig
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.oneconfig.api.commands.v1.CommandManager
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.utils.v1.dsl.createScreen
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
        OmniClientCommands.command(CrashPatchConstants.ID) {
            runs { ctx ->
                ctx.source.openScreen(CrashPatchConfig.createScreen())
            }

            then("reload") {
                runs { ctx ->
                    val success = CrashScanStorage.downloadJson()

                    val content = if (success) {
                        "Successfully reloaded JSON file!" to TextColors.GREEN
                    } else {
                        "Failed to reload JSON file!" to TextColors.RED
                    }

                    val (message, color) = content
                    ctx.source.replyChat(Text.literal("[${CrashPatchConstants.NAME}] $message").setStyle(MCTextStyle.color(color)))
                }
            }

            then("crash") {
                runs { ctx ->
                    requestedCrash = true
                    Command.SINGLE_SUCCESS
                }
            }
        }.register()

        CrashPatchConfig.preload() // Initialize the config
    }

}