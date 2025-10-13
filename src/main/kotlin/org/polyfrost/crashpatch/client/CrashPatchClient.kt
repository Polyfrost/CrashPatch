package org.polyfrost.crashpatch.client

import com.mojang.brigadier.Command
import dev.deftu.omnicore.api.client.commands.OmniClientCommands
import dev.deftu.omnicore.api.client.commands.command
import dev.deftu.omnicore.api.gameDirectory
import dev.deftu.textile.Text
import dev.deftu.textile.minecraft.MCTextStyle
import dev.deftu.textile.minecraft.TextColors
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.oneconfig.utils.v1.Multithreading
import org.polyfrost.oneconfig.utils.v1.dsl.createScreen
import kotlin.io.path.exists

//#if MC < 1.13
import org.polyfrost.crashpatch.client.utils.DeobfuscatingLog4jRewritePolicy
//#endif

object CrashPatchClient {
    private val LOGGER = LogManager.getLogger()

    @JvmStatic
    val isSkyClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        gameDirectory.resolve("OneConfig/CrashPatch/SKYCLIENT").exists() || gameDirectory.resolve("W-OVERFLOW/CrashPatch/SKYCLIENT").exists()
    }

    @JvmStatic
    var isCrashRequested = false

    @JvmStatic
    fun preInitialize() {
        //#if MC <= 1.12.2
        DeobfuscatingLog4jRewritePolicy.install()
        //#endif

        CrashScanner.initialize()

        Multithreading.submit {
            /** Quickly load [isSkyClient] on another temporary thread */
            LOGGER.info("Is this a SkyClient installation? $isSkyClient")
        }

        //#if MC < 1.13
        if (System.getProperty("polyfrost.crashpatch.init_crash") == "true") {
            throw RuntimeException("Crash requested by CrashPatch")
        }
        //#endif
    }

    fun initialize() {
        CrashPatchConfig.preload() // Initialize the config

        OmniClientCommands.command(CrashPatchConstants.ID) {
            runs { ctx ->
                ctx.source.openScreen(CrashPatchConfig.createScreen())
            }

            then("reload") {
                runs { ctx ->
                    val success = CrashScanner.submitCacheRequest()

                    val content = "Requested reload of crash data! Please wait." to TextColors.GREEN
                    val (message, color) = content
                    ctx.source.replyChat(Text.literal("[${CrashPatchConstants.NAME}] $message").setStyle(MCTextStyle.color(color)))
                }
            }

            then("crash") {
                runs { ctx ->
                    isCrashRequested = true
                    Command.SINGLE_SUCCESS
                }
            }
        }.register()
    }
}
