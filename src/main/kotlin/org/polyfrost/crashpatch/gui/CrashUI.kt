package org.polyfrost.crashpatch.gui

import dev.deftu.clipboard.Clipboard
import dev.deftu.omnicore.client.OmniDesktop
import dev.deftu.omnicore.client.OmniScreen
import net.minecraft.client.gui.GuiScreen
import net.minecraft.crash.CrashReport
import org.polyfrost.crashpatch.CrashPatchConstants
import org.polyfrost.crashpatch.client.CrashPatchClient
import org.polyfrost.crashpatch.crashes.CrashScanStorage
import org.polyfrost.crashpatch.crashes.CrashScan
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.crashpatch.utils.UploadUtils
import org.polyfrost.oneconfig.api.ui.v1.Notifications
import org.polyfrost.oneconfig.api.ui.v1.OCPolyUIBuilder
import org.polyfrost.oneconfig.api.ui.v1.UIManager
import org.polyfrost.oneconfig.internal.OneConfig
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.animate.Animations
import org.polyfrost.polyui.color.Colors
import org.polyfrost.polyui.color.PolyColor
import org.polyfrost.polyui.color.rgba
import org.polyfrost.polyui.component.extensions.*
import org.polyfrost.polyui.component.impl.*
import org.polyfrost.polyui.operations.Move
import org.polyfrost.polyui.operations.Resize
import org.polyfrost.polyui.unit.Align
import org.polyfrost.polyui.unit.Vec2
import org.polyfrost.polyui.unit.seconds
import org.polyfrost.polyui.utils.image
import org.polyfrost.polyui.utils.mapToArray
import java.io.File
import java.net.URI
import java.util.function.Consumer

class CrashUI @JvmOverloads constructor(
    private val scanText: String,
    private val file: File?,
    private val susThing: String,
    private val type: GuiType = GuiType.NORMAL,
    val throwable: Throwable? = null
) {

    @JvmOverloads
    constructor(report: CrashReport, type: GuiType = GuiType.NORMAL) : this(
        report
            //#if MC < 1.21
            .completeReport,
            //#else
            //$$ .getFriendlyReport(net.minecraft.ReportType.CRASH),
            //#endif
        report.file
        //#if MC >= 1.21
        //$$ ?.toFile()
        //#endif
        ,
        (report as CrashReportHook).suspectedCrashPatchMods,
        type,
        report.crashCause
    )

    companion object {
        var leaveWorldCrash = false
        var currentInstance: GuiScreen? = null
            private set
        var currentUI: CrashUI? = null
            private set
    }

    init {
        try {
            val initialized = OneConfig::class.java.getDeclaredField("initialized")
            initialized.isAccessible = true
            if (!initialized.getBoolean(OneConfig.INSTANCE)) {
                val registerEventHandlers = OneConfig::class.java.getDeclaredMethod("registerEventHandlers")
                registerEventHandlers.isAccessible = true
                registerEventHandlers.invoke(OneConfig.INSTANCE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val crashScan: CrashScan? by lazy {
        return@lazy CrashScanStorage.scanReport(scanText, type == GuiType.DISCONNECT)
            .let { return@let if (it != null && it.solutions.isNotEmpty()) it else null }
    }
    var shouldCrash = false

    fun create(): GuiScreen {
        val builder = OCPolyUIBuilder.create()
            .blurs()
            .atResolution(1920f, 1080f)
            .backgroundColor(rgba(21, 21, 21))
            .size(650f, 600f)
            .renderer(UIManager.INSTANCE.renderer).translatorDelegate("assets/crashpatch")

        val onClose = Consumer { _: PolyUI ->
            leaveWorldCrash = false
        }

        var selectedSolution: CrashScan.Solution? = crashScan?.solutions?.firstOrNull()
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

                Text(type.title, fontSize = 24f).setFont { PolyUI.defaultFonts.medium }.padded(0f, 10f, 0f, 0f),
                Text("${type.title}.desc.1", fontSize = 14f).setPalette { text.secondary }
                    .padded(0f, if (type != GuiType.INIT || crashScan != null) 16 + 14f else 16f, 0f, 0f),
                if (type != GuiType.INIT || crashScan != null) Text("${type.title}.desc.2", fontSize = 14f).setPalette { text.secondary }
                else null,
                Text(
                    if (type == GuiType.DISCONNECT) "crashpatch.disconnect.cause" else "crashpatch.crash.cause",
                    fontSize = 16f
                ).padded(0f, 24f, 0f, 0f),
                Text(susThing, fontSize = 18f).setFont { PolyUI.defaultFonts.semiBold }.setPalette { brand.fg }
                    .padded(0f, 8f, 0f, 0f),

                Block(
                    Block(
                        // Selector
                        Block(size = Vec2(0f, 2f)).ignoreLayout().radius(2f).afterParentInit(Int.MAX_VALUE) {
                            palette = polyUI.colors.brand.fg
                            selectedSolution?.let { solution ->
                                this.y = parent.y + parent.height - 2f

                                val index = crashScan?.solutions?.indexOf(solution) ?: 0
                                val component = parent[1][index]

                                this.width = component.width
                                this.x = component.x
                            }
                        },

                        // Tabs
                        Group(
                            children = (crashScan?.solutions?.mapToArray { solution ->
                                val rightPad = when {
                                    solution == crashScan?.solutions?.last() -> 0f
                                    else -> 24f
                                }

                                Text(solution.name)
                                    .setFont { PolyUI.defaultFonts.medium }
                                    .padded(0f, 0f, rightPad, 0f)
                                    .onClick {
                                        selectedSolution = solution

                                        parent.parent[0].let { selector ->
                                            Resize(
                                                drawable = selector,
                                                width = width,
                                                add = false,
                                                animation = Animations.EaseInQuad.create(0.15.seconds)
                                            ).add()

                                            Move(
                                                drawable = selector,
                                                x = x,
                                                add = false,
                                                animation = Animations.EaseInQuad.create(0.15.seconds)
                                            ).add()
                                        }

                                        block?.get(1)?.let { group ->
                                            group[0] = createSolutionText(solution)
                                        }

                                        true
                                    }
                            } ?: arrayOf()),
                        ).padded(12f, 0f, 0f, 0f),

                        // Buttons
                        Group(
                            Button(leftImage = "/assets/crashpatch/copy.svg".image()).onClick {
                                selectedSolution?.solutions?.joinToString("\n")?.let(Clipboard.getInstance()::setString).also { copyState ->
                                    if (copyState == true) {
                                        Notifications.enqueue(Notifications.Type.Success, CrashPatchConstants.NAME, "Copied to clipboard!")
                                    }
                                }

                                selectedSolution != null
                            }.setPalette { createCustomButtonPalette(GRAY_600) },
                            Button(leftImage = "/assets/crashpatch/upload.svg".image()).onClick {
                                selectedSolution?.let { solution ->
                                    val link = UploadUtils.upload(solution.solutions.joinToString(separator = "") { it + "\n" } + "\n\n" + (if (!solution.crashReport) scanText else ""))
                                    Clipboard.getInstance().setString(link)

                                    if (OmniDesktop.browse(URI.create(link))) {
                                        Notifications.enqueue(Notifications.Type.Success, CrashPatchConstants.NAME, "Link copied to clipboard and opened in browser")
                                    } else {
                                        Notifications.enqueue(Notifications.Type.Warning, CrashPatchConstants.NAME, "Couldn't open link in browser, copied to clipboard instead.")
                                    }
                                }

                                selectedSolution != null
                            }.setPalette { createCustomButtonPalette(GRAY_600) },
                        ),

                        alignment = Align(
                            pad = Vec2.ZERO,
                            main = Align.Content.SpaceBetween,
                            mode = Align.Mode.Horizontal
                        ),
                        size = Vec2(550f, 37f),
                        color = GRAY_600
                    ).radii(8f, 8f, 0f, 0f),

                    // Selected solution goes here...
                    Group(
                        children = (selectedSolution?.let { arrayOf(createSolutionText(it)) } ?: arrayOf()),
                        alignment = Align(pad = Vec2.ZERO, mode = Align.Mode.Vertical),
                        size = Vec2(518f, 105f),
                    ).padded(16f, 8f),

                    alignment = Align(
                        main = Align.Content.Start,
                        cross = Align.Content.Start,
                        mode = Align.Mode.Vertical,
                        pad = Vec2.ZERO
                    ),
                    size = Vec2(550f, 158f),
                    color = GRAY_700
                ).also { block = it }.padded(0f, 40f, 0f, 0f),

                Text("crashpatch.discord.prompt", fontSize = 16f).padded(0f, 25f, 0f, 0f),
                Group(
                    Image("/assets/crashpatch/discord.svg".image(), size = Vec2(28f, 28f)),
                    Text("crashpatch.link.discord.polyfrost", fontSize = 16f).setPalette { brand.fg }.padded(4f, 0f, 0f, 0f),
                ).onClick {
                    OmniDesktop.browse(URI.create("crashpatch.link.discord.polyfrost"))
                    true
                },

                // Buttons
                Group(
                    Button(text = "crashpatch.continue", padding = Vec2(14f, 14f)).onClick {
                        if (type == GuiType.INIT) {
                            shouldCrash = true
                        } else {
                            OmniScreen.closeScreen()
                        }
                    }.setPalette { brand.fg },
                    Button(
                        text = "crashpatch.log",
                        rightImage = "/assets/crashpatch/open-external.svg".image(),
                        padding = Vec2(14f, 14f)
                    ).onClick {
                        file?.let { OmniDesktop.open(it) }
                        true
                    }.setPalette { createCustomButtonPalette(rgba(21, 21, 21)) },
                ).padded(0f, 32f, 0f, 0f),

                size = Vec2(650f, 600f),
                alignment = Align(pad = Vec2.ZERO, mode = Align.Mode.Vertical)
            ),
        )

        val screen = UIManager.INSTANCE.createPolyUIScreen(polyUI, 1920f, 1080f, false, true, onClose)
        polyUI.window = UIManager.INSTANCE.createWindow()
        currentUI = this
        currentInstance = screen as GuiScreen
        return screen
    }

    private fun createSolutionText(solution: CrashScan.Solution) = Text(
        solution.solutions.joinToString("\n"),
        fontSize = 12f,
        visibleSize = Vec2(518f, 105f),
    ).setFont { PolyUI.monospaceFont }.padded(16f, 8f)

    private fun createCustomButtonPalette(normal: PolyColor) = Colors.Palette(normal, GRAY_700, GRAY_700, PolyColor.TRANSPARENT)

    enum class GuiType(val title: String) {
        INIT("crashpatch.init"), NORMAL("crashpatch.crash"), DISCONNECT("crashpatch.disconnect")
    }
}