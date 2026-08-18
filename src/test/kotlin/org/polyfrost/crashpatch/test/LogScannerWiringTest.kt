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

class LogScannerWiringTest {

    companion object {
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
    fun `ordinary errors are ignored`() {
        LogScanner.install()
        val before = LogScanner.suppressedErrors.size

        LogManager.getLogger("test").error("Unable to get block entity class for \"minecraft:banner\"")

        Assertions.assertEquals(before, LogScanner.suppressedErrors.size)
    }
}
