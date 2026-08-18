package org.polyfrost.crashpatch.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.polyfrost.crashpatch.client.crashes.LogScanner

class LogScannerTest {

    @Test
    fun `mixin apply failures name their mod`() {
        val error = LogScanner.inspect(
            "Mixin apply for mod gtalike_teleport failed gtalike_teleport.mixins.json:" +
                "SodiumDefaultChunkRendererMixin from mod gtalike_teleport -> " +
                "net.caffeinemc.mods.sodium.client.render.chunk.DefaultChunkRenderer: " +
                "org.spongepowered.asm.mixin.injection.throwables.InvalidInjectionException Invalid descriptor",
            null,
        )
        Assertions.assertEquals("gtalike_teleport", error?.mod?.id)
    }

    @Test
    fun `mixins that soft-fail are not blamed`() {
        Assertions.assertNull(
            LogScanner.inspect(
                "@Mixin target net.minecraft.class_332 was not found darkgraph.mixins.json:DrawContextMixin " +
                    "from mod darkgraph",
                null,
            ),
        )
    }

    @Test
    fun `swallowed packet errors keep the whole cause chain`() {
        val root = IllegalStateException("Invalid descriptor on SodiumDefaultChunkRendererMixin")
        val thrown = RuntimeException("Mixin transformation of DefaultChunkRenderer failed", root)

        val lines = LogScanner.inspect(
            "Failed to handle packet ClientboundLoginPacket[playerId=617394], disconnecting",
            thrown,
        )?.lines.orEmpty()

        Assertions.assertTrue(lines[0].startsWith("Failed to handle packet"), lines.toString())
        Assertions.assertTrue(lines[1].contains("Mixin transformation of"), lines.toString())
        Assertions.assertTrue(lines[2].startsWith("Caused by:"), lines.toString())
        Assertions.assertTrue(lines[2].contains("Invalid descriptor"), lines.toString())
    }

    @Test
    fun `cycles in the cause chain terminate`() {
        val inner = RuntimeException("inner")
        val outer = RuntimeException("outer", inner)
        inner.initCause(outer)

        val lines = LogScanner.inspect("Failed to handle packet Whatever, disconnecting", outer)?.lines.orEmpty()
        Assertions.assertTrue(lines.size in 2..10, "was ${lines.size} lines")
    }

    @Test
    fun `packet dumps are truncated`() {
        val message = "Failed to handle packet ClientboundLoginPacket[" + "dimensionType=x, ".repeat(500) +
            "], disconnecting"
        val first = LogScanner.inspect(message, null)?.lines?.first().orEmpty()

        Assertions.assertTrue(first.length < 600, "was ${first.length} chars")
        Assertions.assertTrue(first.endsWith("(truncated)"), first)
    }
}
