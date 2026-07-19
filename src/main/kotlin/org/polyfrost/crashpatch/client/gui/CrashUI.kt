package org.polyfrost.crashpatch.client.gui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.minecraft.CrashReport
import net.minecraft.ReportType
//? if < 26.1 {
/*import net.minecraft.client.gui.GuiGraphics
*///? } else {
import net.minecraft.client.gui.GuiGraphicsExtractor
//? }
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import org.polyfrost.crashpatch.client.LogUploader
import org.polyfrost.crashpatch.client.crashes.CrashScan
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper
import org.polyfrost.oneconfig.internal.OneConfig
import org.polyfrost.oneconfig.internal.ui.compose.BlurRenderer
import org.polyfrost.oneconfig.internal.ui.components.Icon
import org.polyfrost.oneconfig.internal.ui.components.IconButton
import org.polyfrost.oneconfig.internal.ui.components.Text
import org.polyfrost.oneconfig.internal.ui.compose.ComposeScreen
import org.polyfrost.oneconfig.internal.ui.themes.LocalTheme
import org.polyfrost.oneconfig.internal.ui.themes.Theme
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper
import java.awt.Desktop
import java.io.File
import java.net.URI

class CrashUI @JvmOverloads constructor(
    private val scanText: String,
    private val file: File?,
    private val susThing: String,
    private val type: GuiType = GuiType.NORMAL,
    val throwable: Throwable? = null
) : ComposeScreen() {

    val blue = Color(0, 84, 211)
    val lightBlue = Color(40, 155, 255)

    @JvmOverloads
    constructor(report: CrashReport, type: GuiType = GuiType.NORMAL) : this(
        report.getFriendlyReport(ReportType.CRASH),
        report.saveFile?.toFile(),
        (report as CrashReportHook).suspectedMod,
        type,
        report.exception
    )

    companion object {
        var leaveWorldCrash = false
        var currentInstance: Screen? = null
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
        CrashScanner.scan(scanText, type == GuiType.DISCONNECT)
            ?.takeIf { it.solutions.isNotEmpty() }
    }

    var shouldCrash = false

    private var sceneClosed = false

    fun create(): Screen {
        currentUI = this
        currentInstance = this
        return this
    }

    override fun onClose() {
        sceneClosed = true
        super.onClose()
        //? if < 26.2 {
        /*client.setScreen(null)
        *///? } else {
        client.gui.setScreen(null)
        //? }
    }

    override fun removed() {
        leaveWorldCrash = false
        currentInstance = null
        currentUI = null
        super.removed()
    }

    //? if < 26.1 {
    /*override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (sceneClosed) return
        BlurRenderer.drawBlur(8f)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }
    *///? } else {
    override fun extractRenderState(ctx: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, tickDelta: Float) {
        if (sceneClosed) return
        BlurRenderer.drawBlur(8f)
        super.extractRenderState(ctx, mouseX, mouseY, tickDelta)
    }
    //? }

    @Composable
    override fun compose() {
        var selectedSolution by remember(crashScan) {
            mutableStateOf(crashScan?.solutions?.firstOrNull())
        }
        var statusText by remember { mutableStateOf<String?>(null) }
        val tabScroll = rememberScrollState()
        val solutionScroll = rememberScrollState()
        val pageScroll = rememberScrollState()

        Theme {
            val current = LocalTheme.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .width(650.dp)
                        .verticalScroll(pageScroll)
                        .background(current.popupBackground, current.popupShape)
                        .border(1.dp, current.borderColor, current.popupShape)
                        .padding(48.dp, 48.dp, 48.dp, 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        "/assets/crashpatch/WarningTriangle.svg",
                        current.textColor,
                        modifier = Modifier
                            .size(32.dp),
                    )
                    Text(
                        text = translate(type.title),
                        color = current.textColor,
                        fontSize = 24.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = translate("${type.title}.desc.1"),
                        color = current.textColorSecondary,
                        fontSize = 14.sp,
                    )
                    if (type != GuiType.INIT || crashScan != null) {
                        Text(
                            text = translate("${type.title}.desc.2"),
                            color = current.textColorSecondary,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = translate(if (type == GuiType.DISCONNECT) "crashpatch.disconnect.cause" else "crashpatch.crash.cause"),
                        color = current.textColorSecondary,
                        fontSize = 17.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = susThing,
                        color = blue,
                        fontSize = 22.sp,
                    )

                    val solutions = crashScan?.solutions.orEmpty()
                    if (solutions.isNotEmpty()) {
                        val activeSolution = selectedSolution ?: solutions.first()

                        Spacer(Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    current.sidebarBackground,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, current.borderColor, current.popupShape),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp, 12.dp, 12.dp, 0.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .horizontalScroll(tabScroll),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    solutions.forEach { solution ->
                                        val selected = selectedSolution == solution
                                        Column(
                                            modifier = Modifier
                                                .width(IntrinsicSize.Max)
                                                .clickable { selectedSolution = solution },
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(10.dp, 0.dp, 10.dp, 6.dp),
                                            ) {
                                                Text(
                                                    text = solution.name,
                                                    color = current.textColorSecondary,
                                                    fontSize = 13.sp,
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .height(2.dp)
                                                    .fillMaxWidth()
                                                    .background(if (selected) lightBlue else Color.Transparent)
                                            )
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton("/assets/crashpatch/copy.svg") {
                                        ClipboardHelper.setString(activeSolution.solutions.joinToString("\n"))
                                        statusText = "Copied text to clipboard."
                                    }
                                    IconButton("/assets/crashpatch/upload.svg") {
                                        val body = buildString {
                                            append(activeSolution.solutions.joinToString("\n"))
                                            append("\n\n")
                                            if (!activeSolution.isCrashReport) append(scanText)
                                        }
                                        val link = LogUploader.upload(body)
                                        ClipboardHelper.setString(link)
                                        val opened = DesktopHelper.browse(URI.create(link))
                                        statusText = if (opened) {
                                            "Link copied to clipboard and opened in browser"
                                        } else {
                                            "Couldn't open link in browser, copied to clipboard instead."
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(
                                        current.pageBackground,
                                        RoundedCornerShape(
                                            0.dp, 0.dp,
                                            12.dp, 12.dp
                                        )
                                    )
                                    .padding(10.dp),
                            ) {
                                Text(
                                    text = activeSolution.solutions.joinToString("\n"),
                                    color = current.textColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.verticalScroll(solutionScroll),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = translate("crashpatch.discord.prompt"),
                        color = current.textColor,
                        fontSize = 16.sp,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clickable {
                                val opened =
                                    DesktopHelper.browse(URI.create(translate("crashpatch.link.discord.polyfrost")))
                                if (!opened) statusText = "Couldn't open Discord link."
                            },
                    ) {
                        Icon(
                            "/assets/crashpatch/discord.svg",
                            current.textColor,
                            modifier = Modifier
                                .size(28.dp),
                        )
                        Text(
                            text = translate("crashpatch.link.discord.polyfrost"),
                            color = blue,
                            fontSize = 16.sp,
                        )
                    }

                    if (!statusText.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = statusText.orEmpty(),
                            color = current.textColorSecondary,
                            fontSize = 13.sp,
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val continueLabel = if (type == GuiType.INIT) "crashpatch.exit" else "crashpatch.continue"
                        ActionButton(translate(continueLabel), primary = true) {
                            if (type == GuiType.INIT) {
                                shouldCrash = true
                            } else {
                                //? if < 26.2 {
                                /*client.setScreen(null)
                                *///? } else {
                                client.gui.setScreen(null)
                                //? }
                            }
                        }
                        ActionButton({
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    translate("crashpatch.log"),
                                    color = current.textColor,
                                )
                                Icon("/assets/crashpatch/open-external.svg", current.textColor)
                            }
                        }) {
                            val target = file ?: return@ActionButton
                            val opened = DesktopHelper.open(target)
                            if (!opened) statusText = "Couldn't open crash log file."
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ActionButton(
        text: String,
        primary: Boolean = false,
        onClick: () -> Unit,
    ) {
        ActionButton(
            text = { Text(text, color = LocalTheme.current.textColor) },
            primary = primary,
            onClick = onClick,
        )
    }

    @Composable
    private fun ActionButton(
        text: @Composable () -> Unit,
        primary: Boolean = false,
        onClick: () -> Unit,
    ) {
        val current = LocalTheme.current
        Box(
            modifier = Modifier
                .background(
                    if (primary) blue else current.popupBackground,
                    current.buttonShape,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .apply { if (primary) border(1.dp, current.borderColor, current.buttonShape) },
        ) {
            text()
        }
    }

    private fun translate(key: String): String = I18n.get(key)

    enum class GuiType(val title: String) {
        INIT("crashpatch.init"), NORMAL("crashpatch.crash"), DISCONNECT("crashpatch.disconnect")
    }
}