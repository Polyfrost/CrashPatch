package org.polyfrost.crashpatch.gui

import net.minecraft.client.gui.GuiScreen
import net.minecraft.crash.CrashReport
import org.polyfrost.crashpatch.crashes.CrashHelper
import org.polyfrost.crashpatch.crashes.CrashScan
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.oneconfig.api.ui.v1.OCPolyUIBuilder
import org.polyfrost.oneconfig.api.ui.v1.UIManager
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.extensions.named
import org.polyfrost.polyui.component.extensions.padded
import org.polyfrost.polyui.component.extensions.setPalette
import org.polyfrost.polyui.component.impl.Group
import org.polyfrost.polyui.component.impl.Image
import org.polyfrost.polyui.component.impl.Text
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.utils.image
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
            GuiType.INIT -> listOf(SUBTITLE_INIT_1 + (if (crashScan != null) SUBTITLE_INIT_2 else "") + SUBTITLE_INIT_3, "")
            GuiType.NORMAL -> listOf(SUBTITLE_1, SUBTITLE_2)
            GuiType.DISCONNECT -> listOf(SUBTITLE_DISCONNECTED, SUBTITLE_DISCONNECTED_2)
        }
    }

    fun create(): GuiScreen {
        val builder = OCPolyUIBuilder.create()
            .atResolution(1920f, 1080f)
            .blurs()
            .backgroundColor(rgba(21, 21, 21))
            .size(650f, 600f)

        val onClose: Consumer<PolyUI> = Consumer { _: PolyUI ->
            leaveWorldCrash = false
        }

        // builder.onClose(onClose)

        val polyUI = builder.make(
            Group(
                Image("/assets/crashpatch/WarningTriangle.svg".image(Vec2(20F, 20F))).named("WarningTriangle").padded(
                    0F,
                    34F,
                    0F,
                    0F
                ),
                Text(if (type == GuiType.DISCONNECT) DISCONNECTED_TITLE else TITLE, fontSize = 24F, font = PolyUI.defaultFonts.medium).setPalette { text.primary }.padded(0f, 10F, 0f, 0f),
                Text(subtitle[0], fontSize = 14F, font = PolyUI.defaultFonts.regular).setPalette { text.secondary }.padded(0f, 16f, 0f, 0f),
                Text(subtitle[1], fontSize = 14F, font = PolyUI.defaultFonts.regular).setPalette { text.secondary }.padded(0f, 0F, 0f, 0f),
                Text(if (type == GuiType.DISCONNECT) CAUSE_TEXT_DISCONNECTED else CAUSE_TEXT, fontSize = 16F, font = PolyUI.defaultFonts.regular).setPalette { text.primary }.padded(0f, 24F, 0f, 0f),
                Text(susThing, fontSize = 18F, font = PolyUI.defaultFonts.semiBold).setPalette { brand.fg }.padded(0f, 8f, 0f, 0f),
                //Group(
                //    Block(
//
                //    )
                //).padded(0f, 40f, 0f, 0f),
                size = Vec2(650f, 600f),
                alignment = Align(mode = Align.Mode.Vertical)
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