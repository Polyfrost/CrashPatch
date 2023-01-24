package cc.woverflow.crashpatch.gui

import cc.polyfrost.oneconfig.gui.OneConfigGui
import cc.polyfrost.oneconfig.gui.animations.Animation
import cc.polyfrost.oneconfig.gui.animations.ColorAnimation
import cc.polyfrost.oneconfig.gui.animations.EaseOutQuad
import cc.polyfrost.oneconfig.gui.elements.BasicButton
import cc.polyfrost.oneconfig.libs.universal.UDesktop
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowHeight
import cc.polyfrost.oneconfig.libs.universal.UResolution.windowWidth
import cc.polyfrost.oneconfig.platform.Platform
import cc.polyfrost.oneconfig.renderer.NanoVGHelper
import cc.polyfrost.oneconfig.renderer.asset.Icon
import cc.polyfrost.oneconfig.renderer.asset.SVG
import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.renderer.font.FontHelper
import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.renderer.scissor.ScissorHelper
import cc.polyfrost.oneconfig.utils.InputHandler
import cc.polyfrost.oneconfig.utils.NetworkUtils
import cc.polyfrost.oneconfig.utils.Notifications
import cc.polyfrost.oneconfig.utils.color.ColorPalette
import cc.polyfrost.oneconfig.utils.color.ColorUtils
import cc.polyfrost.oneconfig.utils.dsl.*
import cc.polyfrost.oneconfig.utils.gui.OneUIScreen
import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.CrashScan
import cc.woverflow.crashpatch.hooks.CrashReportHook
import cc.woverflow.crashpatch.utils.InternetUtils
import net.minecraft.crash.CrashReport
import java.io.File
import java.net.URI

class CrashGui @JvmOverloads constructor(
    private val scanText: String,
    private val file: File?,
    private val susThing: String,
    private val type: GuiType = GuiType.NORMAL,
    val throwable: Throwable? = null
) : OneUIScreen() {
    @JvmOverloads
    constructor(report: CrashReport, type: GuiType = GuiType.NORMAL) : this(
        report.completeReport,
        report.file,
        (report as CrashReportHook).suspectedCrashPatchMods,
        type,
        report.crashCause
    )

    private val crashPatchLogo = Icon("/assets/crashpatch/crashpatch_dark.svg")

    private var hasteLink: String? = null
    private val crashScan: CrashScan? by lazy {
        return@lazy CrashHelper.scanReport(scanText, type == GuiType.DISCONNECT)
            .let { return@let if (it != null && it.solutions.isNotEmpty()) it else null }
    }
    var shouldCrash = false

    private val subtitle by lazy {
        when (type) {
            GuiType.INIT -> listOf(SUBTITLE_INIT_1 + (if (crashScan != null) SUBTITLE_INIT_2 else "") + SUBTITLE_INIT_3)
            GuiType.NORMAL -> listOf(SUBTITLE_1, SUBTITLE_2)
            GuiType.DISCONNECT -> listOf(SUBTITLE_DISCONNECTED, SUBTITLE_DISCONNECTED_2)
        }
    }

    private val buttonFontSizeField = BasicButton::class.java.getDeclaredField("fontSize").apply { isAccessible = true }

    private val returnToGameButton by lazy {
        val button = BasicButton(
            NanoVGHelper.INSTANCE.getTextWidth(vg, RETURN_TO_GAME, 14f, Fonts.MEDIUM).toInt() + 40,
            40,
            RETURN_TO_GAME,
            2,
            ColorPalette.PRIMARY
        )
        button.setClickAction {
            if (type == GuiType.INIT) {
                shouldCrash = true
            } else {
                restorePreviousScreen()
            }
        }
        buttonFontSizeField.setFloat(button, 14f)
        button
    }

    private val openCrashLogButton by lazy {
        val button = BasicButton(
            NanoVGHelper.INSTANCE.getTextWidth(vg, OPEN_CRASH_LOG, 14f, Fonts.MEDIUM).toInt() + 40 + 5 + 35,
            40,
            OPEN_CRASH_LOG,
            SVG("/assets/crashpatch/open-external.svg"),
            null,
            3,
            ColorPalette.TERTIARY
        )
        button.setClickAction {
            file?.let {
                UDesktop.open(it)
            }
        }
        buttonFontSizeField.setFloat(button, 14f)
        button
    }

    private var scrollAnimation: Animation? = null
    private val colorAnimation = ColorAnimation(
        ColorPalette(
            ColorUtils.getColor(55, 59, 69, 76),
            ColorUtils.getColor(55, 59, 69, 153),
            ColorUtils.getColor(55, 59, 69, 255)
        ), 200
    )
    private var scrollTarget = 0f
    private var scrollTime = 0L
    private var mouseWasDown = false
    private var dragging = false
    private var yStart = 0f
    private var scroll = 0f

    private var lastHeight = 0f

    private var selectedSolution: CrashScan.Solution? = null

    private var vg = -1L

    override fun draw(vg: Long, partialTicks: Float, inputHandler: InputHandler) {
        this.vg = vg
        nanoVG(vg) {
            FontHelper.INSTANCE.loadFont(vg, JETBRAINS_MONO)
            val scale = OneConfigGui.getScaleFactor()
            val x = ((windowWidth - 650 * scale) / 2f / scale).toInt()
            val y = ((windowHeight - 600 * scale) / 2f / scale).toInt()
            scale(scale, scale)
            inputHandler.scale(scale.toDouble(), scale.toDouble())
            drawRoundedRect(x, y, 650, 600, 20, GRAY_800)
            drawSVG("/assets/crashpatch/WarningTriangle.svg", x + 305 + 10, y + 24 + 10, 20, 20)
            drawText(
                if (type == GuiType.DISCONNECT) DISCONNECTED_TITLE else TITLE,
                (windowWidth / 2f / scale) - (getTextWidth(
                    if (type == GuiType.DISCONNECT) DISCONNECTED_TITLE else TITLE,
                    24,
                    Fonts.MEDIUM
                ) / 2f),
                y + 56 + 22,
                WHITE_90,
                24,
                Fonts.MEDIUM
            )
            subtitle.forEachIndexed { index, s ->
                drawText(
                    s,
                    (windowWidth / 2f / scale) - (getTextWidth(s, 14, Fonts.REGULAR) / 2f),
                    y + 56 + 87 + ((index - 1) * (14 * 1.75)),
                    WHITE_80,
                    14,
                    Fonts.REGULAR
                )
            }

            drawText(
                if (type == GuiType.DISCONNECT) CAUSE_TEXT_DISCONNECTED else CAUSE_TEXT,
                (windowWidth / 2f / scale) - (getTextWidth(
                    if (type == GuiType.DISCONNECT) CAUSE_TEXT_DISCONNECTED else CAUSE_TEXT,
                    16,
                    Fonts.REGULAR
                ) / 2f),
                y + 56 + 87 + 10 + (subtitle.size * (14 * 1.75)),
                WHITE_80,
                16,
                Fonts.REGULAR
            )
            drawText(
                susThing, (windowWidth / 2f / scale) - (getTextWidth(
                    susThing, 18, Fonts.SEMIBOLD
                ) / 2f), y + 56 + 87 + 10 + (subtitle.size * (14 * 1.75)) + 30, BLUE_400, 18, Fonts.SEMIBOLD
            )

            drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_700)
            ScissorHelper.INSTANCE.scissor(vg, x + 50f, y + 273f, 550f, 37f).let {
                drawRoundedRect(x + 50, y + 273, 550, 158, 20, GRAY_600)
                ScissorHelper.INSTANCE.resetScissor(vg, it)
            }

            crashScan?.solutions?.let { solutions ->
                var i = 0
                var lastTextWidth = 0f
                solutions.forEach { solution ->
                    i++
                    if (i == 1 && selectedSolution == null) {
                        selectedSolution = solution
                    }
                    drawText(
                        solution.name,
                        x + 50 + 24 + (32 * (i - 1)) + (if (i > 1) lastTextWidth else 0f),
                        y + 273 + 18.5,
                        WHITE_90,
                        12,
                        Fonts.MEDIUM
                    )
                    val textWidth = getTextWidth(solution.name, 12, Fonts.MEDIUM)
                    if (selectedSolution != solution) {
                        if (inputHandler.isAreaClicked(
                                x + 50 + 24 + (32 * (i - 1)) + (if (i > 1) lastTextWidth else 0f),
                                y + 273f,
                                textWidth,
                                37f,
                            )
                        ) {
                            selectedSolution = solution
                            scroll = 0f
                            scrollTarget = 0f
                            scrollTime = 0
                            scrollAnimation = null
                        }
                    }
                    lastTextWidth += textWidth
                    if (selectedSolution == solution) {
                        drawRoundedRect(
                            x + 50 + 24 + (32 * (i - 1)) + (if (i > 1) lastTextWidth - textWidth else 0f),
                            y + 273 + 35,
                            textWidth,
                            2,
                            1,
                            BLUE_400
                        )
                        ScissorHelper.INSTANCE.scissor(vg, x + 50f + 20f, y + 310f + 16, 550f - 20, 89f)
                            .let { scissor ->
                                val scrollBarLength = 89 / lastHeight * 89
                                if (lastHeight > 89) {
                                    scroll = scrollAnimation?.get() ?: scrollTarget
                                    val dWheel = Platform.getMousePlatform().dWheel.toFloat()
                                    if (dWheel != 0f) {
                                        scrollTarget += dWheel
                                        if (scrollTarget > 0f) scrollTarget =
                                            0f else if (scrollTarget < -lastHeight + 89) scrollTarget = -lastHeight + 89
                                        scrollAnimation = EaseOutQuad(150, scroll, scrollTarget, false)
                                        scrollTime = System.currentTimeMillis()
                                    } else if (scrollAnimation != null && scrollAnimation!!.isFinished) scrollAnimation =
                                        null
                                    if (dragging && inputHandler.isClicked(true)) {
                                        dragging = false
                                    }
                                }
                                var height = 0F
                                translate(0f, scroll)
                                solution.solutions.forEach {
                                    height += 12 * 1.25f
                                    drawWrappedString(
                                        it,
                                        x + 50 + 20,
                                        y + 310f + 16 + height,
                                        550 - 20,
                                        WHITE_60,
                                        12,
                                        1.25f,
                                        JETBRAINS_MONO
                                    )
                                    height += NanoVGHelper.INSTANCE.getWrappedStringHeight(
                                        vg, it, 550F, 12F, 1.25f, JETBRAINS_MONO
                                    )
                                }
                                height += 12 * 1.25f
                                translate(0f, -scroll)
                                lastHeight = height
                                ScissorHelper.INSTANCE.resetScissor(vg, scissor)
                                if (lastHeight > 89) {
                                    val scrollBarY = scroll / lastHeight * 81
                                    val isMouseDown = Platform.getMousePlatform().isButtonDown(0)
                                    val scrollHover = inputHandler.isAreaHovered(
                                        (x + 50f + 20f + 530f - 14f),
                                        (y + 310f + 16 - scrollBarY),
                                        12f,
                                        scrollBarLength.toInt().toFloat()
                                    )
                                    val scrollTimePeriod = System.currentTimeMillis() - scrollTime < 1000
                                    if (scrollHover && isMouseDown && !mouseWasDown) {
                                        yStart = inputHandler.mouseY()
                                        dragging = true
                                    }
                                    mouseWasDown = isMouseDown
                                    if (dragging) {
                                        scrollTarget = -(inputHandler.mouseY() - yStart) * lastHeight / 89f
                                        if (scrollTarget > 0f) scrollTarget =
                                            0f else if (scrollTarget < -lastHeight + 89) scrollTarget =
                                            -lastHeight + 89f
                                        scrollAnimation = EaseOutQuad(150, scroll, scrollTarget, false)
                                    }
                                    NanoVGHelper.INSTANCE.drawRoundedRect(
                                        vg,
                                        (x + 50f + 20f + 530f - 14f),
                                        (y + 310f + 16 - scrollBarY),
                                        4f,
                                        scrollBarLength,
                                        colorAnimation.getColor(scrollHover || scrollTimePeriod, dragging),
                                        4f
                                    )
                                }
                            }
                    }
                }
            }
            drawSVG("/assets/crashpatch/upload.svg", x + 600 - 8 - 11 - 15, y + 273 + 11, 15, 15)
            if (inputHandler.isAreaClicked(x + 600 - 8 - 11 - 15f, y + 273 + 11f, 15f, 15f)) {
                selectedSolution?.let { solution ->
                    val link =
                        InternetUtils.upload(solution.solutions.joinToString(separator = "") { it + "\n" } + "\n\n" + (if (!solution.crashReport) scanText else ""))
                    setClipboardString(link)
                    if (UDesktop.browse(URI.create(link))) {
                        Notifications.INSTANCE.send(
                            "CrashPatch", "Link copied to clipboard and opened in browser", crashPatchLogo
                        )
                    } else {
                        Notifications.INSTANCE.send(
                            "CrashPatch", "Couldn't open link in browser, copied to clipboard instead.", crashPatchLogo
                        )
                    }
                }
            }
            drawSVG("/assets/crashpatch/copy.svg", x + 600 - 8 - 11 - 15 - 8 - 11 - 15, y + 273 + 11, 15, 15)
            if (inputHandler.isAreaClicked(x + 600 - 8 - 11 - 15 - 8 - 11 - 15f, y + 273 + 11f, 15f, 15f)) {
                selectedSolution?.let { solution ->
                    setClipboardString(solution.solutions.joinToString(separator = "") { it + "\n" } + "\n\n" + (if (!solution.crashReport) scanText else ""))
                    Notifications.INSTANCE.send("CrashPatch", "Copied to clipboard", crashPatchLogo
                    )
                }
            }

            drawText(
                "If the solution above doesn't help, join", (windowWidth / 2f / scale) - (getTextWidth(
                    "If the solution above doesn't help, join", 16, Fonts.REGULAR
                ) / 2f), y + 273 + 158 + 24 + 20, WHITE_80, 16, Fonts.REGULAR
            )
            val discordMessageWidth = 20 + 15 + getTextWidth("https://inv.wtf/skyclient", 16, Fonts.REGULAR)
            drawSVG(
                "/assets/crashpatch/discord.svg",
                (windowWidth / 2f / scale) - (discordMessageWidth / 2),
                y + 273 + 158 + 24 + 20 + 15,
                20,
                20
            )
            drawURL(
                if (CrashPatch.isSkyclient) SKYCLIENT_DISCORD else POLYFROST_DISCORD,
                (windowWidth / 2f / scale) - (discordMessageWidth / 2) + 20 + 15,
                y + 273 + 158 + 24 + 20 + 15 + 11,
                16,
                Fonts.REGULAR,
                inputHandler
            )

            val buttonsWidth =
                returnToGameButton.width + if (type != GuiType.DISCONNECT) (getTextWidth(
                    OPEN_CRASH_LOG,
                    14,
                    Fonts.MEDIUM
                ) + 16 + 20) + 10 else 0f
            returnToGameButton.update((windowWidth / 2f / scale) - (buttonsWidth / 2), y + 600 - 16 - 36f, inputHandler)
            returnToGameButton.draw(
                vg, (windowWidth / 2f / scale) - (buttonsWidth / 2), y + 600 - 16 - 36f, inputHandler
            )
            if (type != GuiType.DISCONNECT) {
                openCrashLogButton.update(
                    (windowWidth / 2f / scale) - (buttonsWidth / 2) + returnToGameButton.width + 10,
                    y + 600 - 16 - 36f,
                    inputHandler
                )
                openCrashLogButton.draw(
                    vg,
                    (windowWidth / 2f / scale) - (buttonsWidth / 2) + returnToGameButton.width + 10,
                    y + 600 - 16 - 36f,
                    inputHandler
                )
            }
        }
    }

    private fun VG.drawURL(url: String, x: Number, y: Number, size: Int, font: Font, inputHandler: InputHandler) {
        drawText(url, x, y, HYPERLINK_BLUE, size, font)
        val length = getTextWidth(url, size, font)
        if (inputHandler.isAreaHovered(
                (x.toFloat() - 2), (y.toFloat() - size.toFloat()), (length + 4), (size.toFloat() * 2 + 2)
            )
        ) {
            drawRect(x, y.toFloat() + size.toFloat() / 2, length, 2, HYPERLINK_BLUE)
            if (inputHandler.isClicked) {
                NetworkUtils.browseLink(url)
            }
        }
    }

    enum class GuiType {
        INIT, NORMAL, DISCONNECT
    }
}