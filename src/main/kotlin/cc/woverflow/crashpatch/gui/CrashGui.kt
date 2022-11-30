package cc.woverflow.crashpatch.gui

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowHeight
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowWidth
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.*
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.CrashScan
import net.minecraft.crash.CrashReport

class CrashGui @JvmOverloads constructor(val report: CrashReport, init: Boolean = false) : OneUIScreen() {
    private var hasteLink: String? = null
    private val crashScan: CrashScan? by lazy {
        return@lazy CrashHelper.scanReport(report.completeReport).let { return@let if (it != null && it.solutions.isNotEmpty()) it else null }
    }
    var shouldCrash = false
    private var hasteFailed = false

    private val subtitle by lazy {
        if (init) {
            listOf(SUBTITLE_INIT_1 + (if (crashScan != null) SUBTITLE_INIT_2 else "") + SUBTITLE_INIT_3)
        } else {
            listOf(SUBTITLE_1, SUBTITLE_2)
        }
    }

    override fun draw(vg: Long, partialTicks: Float, inputHandler: InputHandler) {
        nanoVG(vg) {
            val scale = OneConfigGui.getScaleFactor()
            val x = ((windowWidth - 650 * scale) / 2f / scale).toInt()
            val y = ((windowHeight - 600 * scale) / 2f / scale).toInt()
            scale(scale, scale)
            inputHandler.scale(scale.toDouble(), scale.toDouble())
            drawRoundedRect(x, y, 650, 600, 20, GRAY_800)
            drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_700)
            ScissorHelper.INSTANCE.scissor(vg, x + 50f, y + 273f, 550f, 37f).let {
                drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_600)
                ScissorHelper.INSTANCE.resetScissor(vg, it)
            }
            drawSVG("/assets/crashpatch/WarningTriangle.svg", x + 305 + 10, y + 24 + 10, 20, 20)
            drawText(TITLE, (windowWidth / 2f / scale) - (getTextWidth(TITLE, 24, Fonts.MEDIUM) / 2f), y + 56 + 24, WHITE_90, 24, Fonts.MEDIUM)
            subtitle.forEachIndexed { index, s -> //todo wtf am i doing here
                drawText(s, (windowWidth / 2f / scale) - (getTextWidth(s, 14, Fonts.REGULAR) / 2f), y + 56 + (24 * 2.5) + 8 + (18.3 * index), WHITE_80, 14, Fonts.REGULAR)
            }
        }
    }
}