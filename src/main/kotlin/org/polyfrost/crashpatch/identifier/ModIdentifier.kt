package org.polyfrost.crashpatch.identifier

import net.minecraft.CrashReport
import org.apache.logging.log4j.LogManager
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.crashes.LogScanner
//? if > 1.8.9 {
import fudge.notenoughcrashes.stacktrace.ModIdentifier as NECModIdentifier
//?} else {
/*import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.metadata.ModOrigin
import java.nio.file.Paths
*///?}

object ModIdentifier {

    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Mod Identifier")

    private val identifying = ThreadLocal.withInitial { false }

    fun identifyFromStacktrace(crashReport: CrashReport, e: Throwable?): ModMetadata? {
        if (identifying.get()) return null
        identifying.set(true)
        return try {
            synchronized(this) {
                //? if > 1.8.9 {
                NECModIdentifier.getSuspectedModsOf(crashReport)?.map { mod ->
                    ModMetadata(
                        mod.id(),
                        mod.name()
                    )
                }?.firstOrNull()
                //?} else
                //identifyFromClasses(e)
            } ?: LogScanner.modFromMixinError(e)
        } catch (t: Throwable) {
            LOGGER.warn("Failed to identify suspected mod from crash report", t)
            LogScanner.modFromMixinError(e)
        } finally {
            identifying.set(false)
        }
    }

    //? if = 1.8.9 {
    /*private fun identifyFromClasses(e: Throwable?): ModMetadata? {
        val mods = FabricLoader.getInstance().allMods
            .filter { it.origin.kind == ModOrigin.Kind.PATH && it.metadata.id != "minecraft" && it.metadata.id != "java" }
        return e?.stackTrace?.take(4)?.filterNot { it.className.startsWith("org.spongepowered.asm.mixin.") }?.firstNotNullOfOrNull { frame ->
            val location = try {
                Class.forName(frame.className, false, ModIdentifier::class.java.classLoader)
                    .protectionDomain?.codeSource?.location?.toURI()?.let(Paths::get)?.toAbsolutePath()
            } catch (_: Throwable) {
                null
            } ?: return@firstNotNullOfOrNull null
            mods.firstOrNull { mod -> mod.origin.paths.any { it.toAbsolutePath() == location } }
                ?.let { ModMetadata(it.metadata.id, it.metadata.name) }
        }
    }
    *///?}
}
