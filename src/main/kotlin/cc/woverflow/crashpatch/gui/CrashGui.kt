package cc.woverflow.crashpatch.gui

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.libs.universal.UDesktop
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowHeight
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowWidth
import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.NetworkUtils
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.dsl.*
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen
import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.CrashScan
import cc.woverflow.crashpatch.hooks.CrashReportHook
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

    private val buttonFontSizeField = BasicButton::class.java.getDeclaredField("fontSize").apply { isAccessible = true }

    private val returnToGameButton by lazy {
        val button = BasicButton(NanoVGHelper.INSTANCE.getTextWidth(vg, RETURN_TO_GAME, 14f, Fonts.MEDIUM).toInt() + 40, 40, RETURN_TO_GAME, 2, ColorPalette.PRIMARY)
        button.setClickAction {
            if (init) {
                shouldCrash = true
            } else {
                restorePreviousScreen()
            }
        }
        buttonFontSizeField.setFloat(button, 14f)
        button
    }

    private val openCrashLogButton by lazy {
        val button = BasicButton(NanoVGHelper.INSTANCE.getTextWidth(vg, OPEN_CRASH_LOG, 14f, Fonts.MEDIUM).toInt() + 40 + 5 + 35, 40, OPEN_CRASH_LOG, SVG("/assets/crashpatch/open-external.svg"), null, 3, ColorPalette.TERTIARY)
        button.setClickAction {
            UDesktop.open(report.file)
        }
        buttonFontSizeField.setFloat(button, 14f)
        button
    }

    private var vg = -1L

    override fun draw(vg: Long, partialTicks: Float, inputHandler: InputHandler) {
        this.vg = vg
        nanoVG(vg) {
            val scale = OneConfigGui.getScaleFactor()
            val x = ((windowWidth - 650 * scale) / 2f / scale).toInt()
            val y = ((windowHeight - 600 * scale) / 2f / scale).toInt()
            scale(scale, scale)
            inputHandler.scale(scale.toDouble(), scale.toDouble())
            drawRoundedRect(x, y, 650, 600, 20, GRAY_800)
            drawSVG("/assets/crashpatch/WarningTriangle.svg", x + 305 + 10, y + 24 + 10, 20, 20)
            drawText(TITLE, (windowWidth / 2f / scale) - (getTextWidth(TITLE, 24, Fonts.MEDIUM) / 2f), y + 56 + 22, WHITE_90, 24, Fonts.MEDIUM)
            subtitle.forEachIndexed { index, s ->
                drawText(s, (windowWidth / 2f / scale) - (getTextWidth(s, 14, Fonts.REGULAR) / 2f), y + 56 + 87 + ((index - 1) * (14 * 1.75)), WHITE_80, 14, Fonts.REGULAR)
            }

            drawText(CAUSE_TEXT, (windowWidth / 2f / scale) - (getTextWidth(CAUSE_TEXT, 16, Fonts.REGULAR) / 2f), y + 56 + 87 + 10 + (subtitle.size * (14 * 1.75)), WHITE_80, 16, Fonts.REGULAR)
            drawText((report as CrashReportHook).suspectedCrashPatchMods, (windowWidth / 2f / scale) - (getTextWidth((report as CrashReportHook).suspectedCrashPatchMods, 18, Fonts.SEMIBOLD) / 2f), y + 56 + 87 + 10 + (subtitle.size * (14 * 1.75)) + 30, BLUE_400, 18, Fonts.SEMIBOLD)

            drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_700)
            ScissorHelper.INSTANCE.scissor(vg, x + 50f, y + 273f, 550f, 37f).let {
                drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_600)
                ScissorHelper.INSTANCE.resetScissor(vg, it)
            }

            drawText("Crash log", x + 50 + 24, y + 273 + 18.5, WHITE_90, 12, Fonts.MEDIUM)
            //crashScan?.solutions?.let {
            //
            //}
            var i = 0
            var lastTextWidth: Float
            mapOf(
                Pair("Solutions", mutableListOf("Don't crash your game", "You idiot")),
                Pair("Warnings", mutableListOf("Maybe you should stop playing Minecraft", "You're stupid"))
            ).forEach { (t, u) ->
                i++
                lastTextWidth = getTextWidth(t, 12, Fonts.MEDIUM)
                drawText(t, x + 50 + 24 + getTextWidth("Crash log", 12, Fonts.MEDIUM) + (32 * i) + (if (i > 1) lastTextWidth else 0f), y + 273 + 18.5, WHITE_90, 12, Fonts.MEDIUM)
            }
            drawSVG("/assets/crashpatch/upload.svg", x + 600 - 8 - 11 - 15, y + 273 + 11, 15, 15)
            drawSVG("/assets/crashpatch/copy.svg", x + 600 - 8 - 11 - 15 - 8 - 11 - 15, y + 273 + 11, 15, 15)

            drawText("If the solution above doesn't help, join", (windowWidth / 2f / scale) - (getTextWidth("If the solution above doesn't help, join", 16, Fonts.REGULAR) / 2f), y + 273 + 158 + 24 + 20, WHITE_80, 16, Fonts.REGULAR)
            val discordMessageWidth = 20 + 15 + getTextWidth("https://inv.wtf/skyclient", 16, Fonts.REGULAR)
            drawSVG("/assets/crashpatch/discord.svg", (windowWidth / 2f / scale) - (discordMessageWidth / 2), y + 273 + 158 + 24 + 20 + 15, 20, 20)
            drawURL(if (CrashPatch.isSkyclient) SKYCLIENT_DISCORD else POLYFROST_DISCORD, (windowWidth / 2f / scale) - (discordMessageWidth / 2) + 20 + 15, y + 273 + 158 + 24 + 20 + 15 + 11, 16, Fonts.REGULAR, inputHandler)

            val buttonsWidth = returnToGameButton.width + (getTextWidth(OPEN_CRASH_LOG, 14, Fonts.MEDIUM) + 16 + 20) + 10
            returnToGameButton.update((windowWidth / 2f / scale) - (buttonsWidth / 2), y + 600 - 16 - 36f, inputHandler)
            returnToGameButton.draw(vg, (windowWidth / 2f / scale) - (buttonsWidth / 2), y + 600 - 16 - 36f, inputHandler)
            openCrashLogButton.update((windowWidth / 2f / scale) - (buttonsWidth / 2) + returnToGameButton.width + 10, y + 600 - 16 - 36f, inputHandler)
            openCrashLogButton.draw(vg, (windowWidth / 2f / scale) - (buttonsWidth / 2) + returnToGameButton.width + 10, y + 600 - 16 - 36f, inputHandler)
        }
    }

    private fun VG.drawURL(url: String, x: Number, y: Number, size: Int, font: Font, inputHandler: InputHandler) {
        drawText(url, x, y, HYPERLINK_BLUE, size, font)
        val length = getTextWidth(url, size, font)
        if (inputHandler.isAreaHovered(
                (x.toFloat() - 2),
                (y.toFloat() - size.toFloat()),
                (length + 4),
                (size.toFloat() * 2 + 2)
            )
        ) {
            drawRect(x, y.toFloat() + size.toFloat() / 2, length, 2, HYPERLINK_BLUE)
            if (inputHandler.isClicked) {
                NetworkUtils.browseLink(url)
            }
        }
    }
}