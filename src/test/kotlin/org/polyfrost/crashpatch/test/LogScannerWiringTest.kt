package org.polyfrost.crashpatch.test

import net.minecraft.CrashReport
import net.minecraft.ReportType
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.polyfrost.crashpatch.client.crashes.LogScanner
import org.polyfrost.crashpatch.identifier.ModIdentifier
import java.util.concurrent.atomic.AtomicBoolean

class LogScannerWiringTest {

    companion object {
        private const val SECTION_HEADER = "Errors before the crash (found by CrashPatch)"

        @JvmStatic
        @org.junit.jupiter.api.BeforeAll
        fun setupEnvironment() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `errors logged through log4j reach the crash report`() {
        LogScanner.install()

        LogManager.getLogger("mixin").log(
            Level.ERROR,
            "Mixin apply for mod examplemod failed examplemod.mixins.json:ExampleMixin from mod examplemod -> " +
                "net.minecraft.client.Minecraft: org.spongepowered.asm.mixin.injection.throwables." +
                "InvalidInjectionException Invalid descriptor",
            null as Throwable?,
        )

        val captured = LogScanner.suppressedErrors.firstOrNull { it.mod?.id == "examplemod" }
        Assertions.assertNotNull(captured, "appender never saw the error: ${LogScanner.suppressedErrors}")

        val report = CrashReport("Unexpected error", RuntimeException("downstream NPE"))
            .getFriendlyReport(ReportType.CRASH)
        Assertions.assertTrue(report.contains("Errors before the crash"), "no section in report")
        Assertions.assertTrue(report.contains("Mixin apply for mod examplemod failed"), "no error in report")
    }

    @Test
    fun `mod is still named when the identifier itself blows up`() {
        val thrown = RuntimeException(
            "Mixin transformation of net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager failed",
            RuntimeException(
                "Mixin [axiom.mixins.json:compat.MixinSodiumRenderSectionManager from mod axiom] from phase " +
                    "[DEFAULT] in config [axiom.mixins.json] FAILED during APPLY",
            ),
        )

        Assertions.assertEquals(
            "axiom",
            ModIdentifier.identifyFromStacktrace(CrashReport("Unexpected error", thrown), thrown)?.id,
        )
    }

    @Test
    fun `our section does not steal the head stacktrace`() {
        LogScanner.install()
        val errors = LogScanner.suppressedErrors as MutableList<LogScanner.SuppressedError>
        val saved = errors.toList()
        try {
            for (withCategory in listOf(true, false)) {
                errors.clear()
                val before = headSection(freshReport(withCategory))
                Assertions.assertEquals(
                    withCategory,
                    before.isNotEmpty(),
                    "unexpected head to begin with (pre-existing category = $withCategory): $before",
                )

                errors += LogScanner.SuppressedError(null, listOf("Mixin apply for mod examplemod failed"))
                val full = freshReport(withCategory)

                Assertions.assertEquals(
                    before,
                    headSection(full),
                    "head changed (pre-existing category = $withCategory)",
                )
                Assertions.assertTrue(
                    full.contains("Mixin apply for mod examplemod failed"),
                    "section missing from report (pre-existing category = $withCategory)",
                )
            }
        } finally {
            errors.clear()
            errors += saved
        }
    }

    private fun freshReport(withCategory: Boolean): String {
        val report = CrashReport("Unexpected error", RuntimeException("boom"))
        if (withCategory) report.addCategory("Head")
        return report.getFriendlyReport(ReportType.CRASH)
    }

    private fun headSection(report: String): String =
        report.substringAfter("-- Head --\n", "")
            .lineSequence()
            .takeWhile { it.isNotBlank() }
            .joinToString("\n")

    @Test
    fun `a retried install still records each error once`() {
        LogScanner.install()
        val installed = LogScanner::class.java.getDeclaredField("installed")
            .apply { isAccessible = true }
            .get(LogScanner) as AtomicBoolean
        installed.set(false)
        LogScanner.install()

        withNoRecordedErrors { errors ->
            LogManager.getLogger("mixin").log(
                Level.ERROR,
                "Mixin apply for mod twicemod failed twicemod.mixins.json:TwiceMixin from mod twicemod",
                null as Throwable?,
            )
            Assertions.assertEquals(1, errors.size, "recorded $errors")
        }
    }

    @Test
    fun `every render of a report carries the section exactly once`() {
        withNoRecordedErrors { errors ->
            errors += LogScanner.SuppressedError(null, listOf("Mixin apply for mod examplemod failed"))

            val report = CrashReport("Unexpected error", RuntimeException("boom"))
            val first = report.getFriendlyReport(ReportType.CRASH)
            val second = report.getFriendlyReport(ReportType.CRASH)

            Assertions.assertEquals(1, first.sections(), "first render:\n$first")
            Assertions.assertEquals(1, second.sections(), "second render:\n$second")
        }
    }

    private fun String.sections(): Int = split(SECTION_HEADER).size - 1

    private fun withNoRecordedErrors(block: (MutableList<LogScanner.SuppressedError>) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        val errors = LogScanner.suppressedErrors as MutableList<LogScanner.SuppressedError>
        val saved = errors.toList()
        errors.clear()
        try {
            block(errors)
        } finally {
            errors.clear()
            errors += saved
        }
    }

    @Test
    fun `ordinary errors are ignored`() {
        LogScanner.install()
        val before = LogScanner.suppressedErrors.size

        LogManager.getLogger("test").error("Unable to get block entity class for \"minecraft:banner\"")

        Assertions.assertEquals(before, LogScanner.suppressedErrors.size)
    }
}
