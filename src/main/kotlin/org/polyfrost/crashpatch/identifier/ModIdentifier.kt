package org.polyfrost.crashpatch.identifier

import net.minecraft.CrashReport
import fudge.notenoughcrashes.stacktrace.ModIdentifier as NECModIdentifier

object ModIdentifier {

    fun identifyFromStacktrace(crashReport: CrashReport, e: Throwable?): ModMetadata? {
        return NECModIdentifier.getSuspectedModsOf(crashReport)?.map { mod ->
            ModMetadata(
                mod.id(),
                mod.name()
            )
        }?.firstOrNull()
    }
}