package cc.woverflow.crashpatch.gui

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowHeight
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowWidth
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.dsl.drawRoundedRect
import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import cc.polyfrost.oneconfig.utils.dsl.scale
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen
import net.minecraft.crash.CrashReport

class CrashGui @JvmOverloads constructor(val report: CrashReport, private val init: Boolean = false) : OneUIScreen() {
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

        }
    }
}