package org.polyfrost.crashpatch.client

import io.sentry.Sentry
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants

object SentryManager {

    private const val DSN =
        "https://0ad6543f59cace39c3ecb7ab9d62c565@o4511714343124992.ingest.us.sentry.io/4511759290597376"

    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Sentry")

    val enabled: Boolean by lazy { FabricLoader.getInstance().isModLoaded("polyplus") }

    private val minecraftVersion: String by lazy {
        FabricLoader.getInstance().getModContainer("minecraft")
            .map { it.metadata.version.friendlyString }
            .orElse("unknown")
    }

    @Volatile
    private var initialized = false

    private fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            Sentry.init { options ->
                options.dsn = DSN
                options.release = "${CrashPatchConstants.ID}@${CrashPatchConstants.VERSION}"
            }
            initialized = true
        }
    }

    fun capture(throwable: Throwable, suspectedMod: String?, crashType: String) {
        if (!enabled) return
        try {
            ensureInitialized()
            Sentry.withScope { scope ->
                scope.setTag("crash_type", crashType)
                scope.setTag("minecraft_version", minecraftVersion)
                if (!suspectedMod.isNullOrBlank()) {
                    scope.setTag("suspected_mod", suspectedMod)
                }
                Sentry.captureException(throwable)
            }
        } catch (t: Throwable) {
            LOGGER.error("Failed to report crash to Sentry", t)
        }
    }
}
