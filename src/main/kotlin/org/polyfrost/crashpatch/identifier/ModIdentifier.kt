package org.polyfrost.crashpatch.identifier

import net.minecraft.CrashReport
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants
import fudge.notenoughcrashes.stacktrace.ModIdentifier as NECModIdentifier

object ModIdentifier {

    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Mod Identifier")

    private val identifying = ThreadLocal.withInitial { false }

    fun identifyFromStacktrace(crashReport: CrashReport, e: Throwable?): ModMetadata? {
        if (identifying.get()) return null
        identifying.set(true)
        return try {
            synchronized(this) {
                NECModIdentifier.getSuspectedModsOf(crashReport)?.map { mod ->
                    ModMetadata(
                        mod.id(),
                        mod.name()
                    )
                }?.firstOrNull()
            }
        } catch (t: Throwable) {
            LOGGER.warn("Failed to identify suspected mod from crash report", t)
            null
        } finally {
            identifying.set(false)
        }
    }
}