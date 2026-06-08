package org.polyfrost.crashpatch.client.gui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.minecraft.CrashReport
import net.minecraft.ReportType
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import org.polyfrost.crashpatch.client.LogUploader
import org.polyfrost.crashpatch.client.crashes.CrashScan
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper
import org.polyfrost.oneconfig.internal.OneConfig
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

    fun create(): Screen {
        currentUI = this
        currentInstance = this
        return this
    }

    override fun removed() {
        leaveWorldCrash = false
        currentInstance = null
        currentUI = null
        super.removed()
    }

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
                    .background(current.pageBackground)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .verticalScroll(pageScroll)
                        .background(current.popupBackground, current.popupShape)
                        .border(1.dp, current.borderColor, current.popupShape)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon("/assets/crashpatch/WarningTriangle.svg", Color(0xFFFF5555))
                    Text(
                        text = translate(type.title),
                        color = Color(0xFFFF5555),
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
                        color = current.textColor,
                        fontSize = 17.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = susThing,
                        color = Color(0xFFFF5555),
                        fontSize = 22.sp,
                    )

                    val solutions = crashScan?.solutions.orEmpty()
                    if (solutions.isNotEmpty()) {
                        val activeSolution = selectedSolution ?: solutions.first()

                        Spacer(Modifier.height(24.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(current.sidebarBackground, current.popupShape)
                                .border(1.dp, current.borderColor, current.popupShape)
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(tabScroll),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                solutions.forEach { solution ->
                                    val selected = selectedSolution == solution
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (selected) current.textColorSecondary else current.pageBackground,
                                                shape = current.buttonShape,
                                            )
                                            .clickable { selectedSolution = solution }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text = solution.name,
                                            color = if (selected) current.accentTextColor else current.textColorSecondary,
                                            fontSize = 13.sp,
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(current.pageBackground, current.popupShape)
                                    .padding(10.dp),
                            ) {
                                Text(
                                    text = activeSolution.solutions.joinToString("\n"),
                                    color = current.textColor,
                                    fontSize = 12.sp,
                                    modifier = Modifier.verticalScroll(solutionScroll),
                                )
                            }
                            Spacer(Modifier.height(12.dp))
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
                    }

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = translate("crashpatch.discord.prompt"),
                        color = current.textColor,
                        fontSize = 15.sp,
                    )
                    Text(
                        text = translate("crashpatch.link.discord.polyfrost"),
                        color = Color(0xFF6BA6FF),
                        fontSize = 15.sp,
                        modifier = Modifier.clickable {
                            val opened =
                                DesktopHelper.browse(URI.create(translate("crashpatch.link.discord.polyfrost")))
                            if (!opened) statusText = "Couldn't open Discord link."
                        },
                    )

                    if (!statusText.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = statusText.orEmpty(),
                            color = current.textColorSecondary,
                            fontSize = 13.sp,
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ActionButton(translate("crashpatch.continue"), highlighted = true) {
                            if (type == GuiType.INIT) {
                                shouldCrash = true
                            } else {
                                client.setScreen(null)
                            }
                        }
                        ActionButton(translate("crashpatch.log")) {
                            val target = file ?: return@ActionButton
                            val opened = DesktopHelper.executeIfDesktop(Desktop.Action.OPEN) { it.open(target) }
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
        highlighted: Boolean = false,
        onClick: () -> Unit,
    ) {
        val current = LocalTheme.current
        Box(
            modifier = Modifier
                .background(
                    if (highlighted) current.modCardBackground else current.pageBackground,
                    current.buttonShape,
                )
                .border(1.dp, current.borderColor, current.buttonShape)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = text,
                color = current.textColor,
                fontSize = 14.sp,
            )
        }
    }

    private fun translate(key: String): String = I18n.get(key)

    enum class GuiType(val title: String) {
        INIT("crashpatch.init"), NORMAL("crashpatch.crash"), DISCONNECT("crashpatch.disconnect")
    }
}