package org.polyfrost.crashpatch.gui

import dev.deftu.clipboard.Clipboard
import net.minecraft.client.gui.GuiScreen
import net.minecraft.crash.CrashReport
import org.polyfrost.crashpatch.crashes.CrashHelper
import org.polyfrost.crashpatch.crashes.CrashScan
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.oneconfig.api.ui.v1.OCPolyUIBuilder
import org.polyfrost.oneconfig.api.ui.v1.UIManager
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.Component
import org.polyfrost.polyui.component.extensions.*
import org.polyfrost.polyui.component.impl.*
import org.polyfrost.polyui.data.PolyImage
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.unit.Vec4
import org.polyfrost.polyui.utils.image
import org.polyfrost.polyui.utils.mapToArray
import java.io.File
import java.util.function.Consumer

class CrashGuiRewrite @JvmOverloads constructor(
    private val scanText: String,
    private val file: File?,
    private val susThing: String,
    private val type: GuiType = GuiType.NORMAL,
    val throwable: Throwable? = null
) {

    @JvmOverloads
    constructor(report: CrashReport, type: GuiType = GuiType.NORMAL) : this(
        report.completeReport,
        report.file,
        (report as CrashReportHook).suspectedCrashPatchMods,
        type,
        report.crashCause
    )

    companion object {
        var leaveWorldCrash = false
    }

    private val crashScan: CrashScan? by lazy {
        return@lazy CrashHelper.scanReport(scanText, type == GuiType.DISCONNECT)
            .let { return@let if (it != null && it.solutions.isNotEmpty()) it else null }
    }
    var shouldCrash = false

    private val subtitle by lazy {
        when (type) {
            GuiType.INIT -> listOf(
                SUBTITLE_INIT_1 + (if (crashScan != null) SUBTITLE_INIT_2 else "") + SUBTITLE_INIT_3,
                ""
            )

            GuiType.NORMAL -> listOf(SUBTITLE_1, SUBTITLE_2)
            GuiType.DISCONNECT -> listOf(SUBTITLE_DISCONNECTED, SUBTITLE_DISCONNECTED_2)
        }
    }

    fun create(): GuiScreen {
        val builder = OCPolyUIBuilder.create()
            .blurs()
            .atResolution(1920f, 1080f)
            .backgroundColor(rgba(21, 21, 21))
            .size(650f, 600f)
            .renderer(UIManager.INSTANCE.renderer)

        val onClose: Consumer<PolyUI> = Consumer { _: PolyUI ->
            leaveWorldCrash = false
        }

        var selectedSolution: CrashScan.Solution? = null
        var block: Block? = null

        val polyUI = builder.make(
            Group(
                Image("/assets/crashpatch/WarningTriangle.svg".image(), size = Vec2(20F, 20F)).named("WarningTriangle")
                    .padded(
                        0F,
                        34F,
                        0F,
                        0F
                    ),
                Text(
                    if (type == GuiType.DISCONNECT) DISCONNECTED_TITLE else TITLE,
                    fontSize = 24F,
                ).setFont { PolyUI.defaultFonts.medium }.padded(0f, 10F, 0f, 0f),
                Text(subtitle[0], fontSize = 14F).setPalette { text.secondary }
                    .padded(0f, 16f, 0f, 0f),
                Text(subtitle[1], fontSize = 14F).setPalette { text.secondary }
                    .padded(0f, 0F, 0f, 0f),
                Text(
                    if (type == GuiType.DISCONNECT) CAUSE_TEXT_DISCONNECTED else CAUSE_TEXT,
                    fontSize = 16F
                ).padded(0f, 24F, 0f, 0f),
                Text(susThing, fontSize = 18F).setFont { PolyUI.defaultFonts.semiBold }.setPalette { brand.fg }
                    .padded(0f, 8f, 0f, 0f),
                Block(
                    Block(
                        // Tabs
                        Group(
                            children = crashScan?.solutions?.mapToArray { solution ->
                                val rightPad = when {
                                    solution == crashScan?.solutions?.last() -> 0f
                                    else -> 32f
                                }

                                Text(solution.name)
                                    .setFont { PolyUI.defaultFonts.medium }
                                    .padded(0f, 0f, rightPad, 0f)
                                    .onClick {
                                        selectedSolution = solution
                                        block?.set(
                                            1,
                                            Text(
                                                solution.solutions.joinToString("\n"),
                                                fontSize = 12f,
                                                visibleSize = Vec2(550f, 121f),
                                            ).padded(16f, 8f)
                                        )

                                        true
                                    }
                            } ?: arrayOf(),
                        ).padded(24f, 0f, 0f, 0f),

                        // Buttons
                        Group(
                            Button(leftImage = "/assets/crashpatch/copy.svg".image()).onClick {
                                selectedSolution?.solutions?.joinToString("\n")?.let(Clipboard.getInstance()::setString)
                                selectedSolution != null
                            }.setPalette { component.bg },
                            Button(leftImage = "/assets/crashpatch/upload.svg".image()).padded(8f, 0f, 0f, 0f),
                        ),

                        alignment = Align(
                            pad = Vec2.ZERO,
                            main = Align.Main.SpaceBetween,
                            mode = Align.Mode.Horizontal
                        ),
                        size = Vec2(550f, 37f),
                        color = GRAY_600
                    ).radii(8f, 8f, 0f, 0f),

                    // Selected solution goes here...
                    Group().padded(16f, 8f),

                    alignment = Align(
                        main = Align.Main.Start,
                        cross = Align.Cross.Start,
                        mode = Align.Mode.Vertical,
                        pad = Vec2.ZERO
                    ),
                    size = Vec2(550f, 158f),
                    color = GRAY_700
                ).also { block = it }.padded(0f, 40f, 0f, 0f),

                size = Vec2(650f, 600f),
                alignment = Align(pad = Vec2.ZERO, mode = Align.Mode.Vertical)
            ),
        )

        val screen =
            UIManager.INSTANCE.createPolyUIScreen(polyUI, 1920f, 1080f, false, true, onClose)
        polyUI.window = UIManager.INSTANCE.createWindow()
        return screen as GuiScreen
    }

    enum class GuiType {
        INIT, NORMAL, DISCONNECT
    }
}