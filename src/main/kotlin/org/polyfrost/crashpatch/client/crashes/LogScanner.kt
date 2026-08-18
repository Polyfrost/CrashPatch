package org.polyfrost.crashpatch.client.crashes

import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.identifier.ModMetadata
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

object LogScanner {

    private val LOGGER = LogManager.getLogger("${CrashPatchConstants.NAME} / Log Scan")

    private val MIXIN_FAILURE = Regex("""Mixin (?:apply|prepare) for mod (\S+) failed""")

    private val SWALLOWED_PACKET = Regex("""Failed to handle packet .*disconnecting""")

    private val UNHELPFUL_SUSPECTS = setOf("unknown", "minecraft", "not enough crashes", "crashpatch")

    private const val MAX_ERRORS = 5
    private const val MAX_CAUSES = 8
    private const val MAX_LINE_LENGTH = 500

    data class SuppressedError(val mod: ModMetadata?, val lines: List<String>)

    val suppressedErrors: List<SuppressedError> = CopyOnWriteArrayList()

    private val installed = AtomicBoolean()

    @JvmStatic
    fun install() {
        if (!installed.compareAndSet(false, true)) return
        try {
            val root = LogManager.getRootLogger()
            if (root !is Logger) {
                LOGGER.warn("Root logger is a ${root.javaClass.name}, so suppressed errors won't be collected")
                return
            }
            root.addAppender(Listener().apply { start() })
        } catch (t: Throwable) {
            LOGGER.warn("Failed to listen for suppressed errors", t)
        }
    }

    fun failedModId(message: String): String? = MIXIN_FAILURE.find(message)?.groupValues?.get(1)

    @JvmStatic
    fun refineSuspect(reported: String): String {
        if (reported.lowercase() !in UNHELPFUL_SUSPECTS) return reported
        return suppressedErrors.firstNotNullOfOrNull { it.mod }?.name ?: reported
    }

    @JvmStatic
    fun reportLines(): List<String> = suppressedErrors.flatMap { it.lines }

    fun inspect(message: String, thrown: Throwable?): SuppressedError? {
        val modId = failedModId(message)
        if (modId == null && !SWALLOWED_PACKET.containsMatchIn(message)) return null

        val lines = mutableListOf(truncate(message))
        var cause = thrown
        var depth = 0
        while (cause != null && depth < MAX_CAUSES) {
            lines += truncate(if (depth == 0) cause.toString() else "Caused by: $cause")
            val next = cause.cause
            if (next === cause) break
            cause = next
            depth++
        }

        return SuppressedError(modId?.let { ModMetadata(it, modName(it)) }, lines)
    }

    private fun truncate(line: String): String =
        if (line.length <= MAX_LINE_LENGTH) line else line.take(MAX_LINE_LENGTH) + "... (truncated)"

    private fun modName(id: String): String = try {
        FabricLoader.getInstance().getModContainer(id).map { it.metadata.name }.orElse(id)
    } catch (t: Throwable) {
        id
    }

    private class Listener : AbstractAppender(CrashPatchConstants.NAME, null, null, true, Property.EMPTY_ARRAY) {
        override fun append(event: LogEvent) {
            if (suppressedErrors.size >= MAX_ERRORS) return
            if (!event.level.isMoreSpecificThan(Level.ERROR)) return

            try {
                val error = inspect(event.message.formattedMessage, event.thrown) ?: return
                (suppressedErrors as MutableList) += error
            } catch (t: Throwable) {
                LOGGER.warn("Failed to inspect a log event", t)
            }
        }
    }
}
