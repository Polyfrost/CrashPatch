@file:OptIn(ExperimentalOneConfigUiApi::class)

package org.polyfrost.crashpatch.client.gui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.unit.dp
import com.google.gson.JsonParser
import net.minecraft.CrashReport
//? if > 1.8.9
import net.minecraft.ReportType
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.language.I18n
import org.polyfrost.crashpatch.client.LogUploader
import org.polyfrost.crashpatch.client.crashes.CrashScan
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.crashpatch.client.crashes.LogScanner
import org.polyfrost.crashpatch.hooks.CrashReportHook
import org.polyfrost.oneconfig.api.notifications.v1.NotificationType
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.OneConfigPrefs
import org.polyfrost.oneconfig.api.ui.v1.a11y.A11yRole
import org.polyfrost.oneconfig.api.ui.v1.components.ButtonDefaults
import org.polyfrost.oneconfig.api.ui.v1.components.CenteredPanel
import org.polyfrost.oneconfig.api.ui.v1.components.CodeBlock
import org.polyfrost.oneconfig.api.ui.v1.components.ControlSize
import org.polyfrost.oneconfig.api.ui.v1.components.ControlStyle
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.IconButton
import org.polyfrost.oneconfig.api.ui.v1.components.LinkText
import org.polyfrost.oneconfig.api.ui.v1.components.ScrollContainer
import org.polyfrost.oneconfig.api.ui.v1.components.StatusText
import org.polyfrost.oneconfig.api.ui.v1.components.TabItem
import org.polyfrost.oneconfig.api.ui.v1.components.TabRow
import org.polyfrost.oneconfig.api.ui.v1.components.TabStyle
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.TextButton
import org.polyfrost.oneconfig.api.ui.v1.components.oneConfigClickable
import org.polyfrost.oneconfig.api.ui.v1.components.rememberStatusMessage
import org.polyfrost.oneconfig.api.ui.v1.platform.OneConfigComposeScreen
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme
import org.polyfrost.oneconfig.api.ui.v1.theme.legibleOn
import org.polyfrost.oneconfig.utils.v1.ClipboardHelper
import java.io.File
import java.net.URI

class CrashUI @JvmOverloads constructor(
    private val scanText: String,
    private val file: File?,
    private val susThing: String,
    private val type: GuiType = GuiType.NORMAL,
    val throwable: Throwable? = null
) : OneConfigComposeScreen(previous = null) {

    @JvmOverloads
    constructor(report: CrashReport, type: GuiType = GuiType.NORMAL) : this(
        //? if > 1.8.9 {
        report.getFriendlyReport(ReportType.CRASH),
        report.saveFile?.toFile(),
        //?} else {
        /*report.build(),
        report.file,
        *///?}
        LogScanner.refineSuspect((report as CrashReportHook).suspectedMod),
        type,
        report.exception
    )

    companion object {
        var leaveWorldCrash = false
        var currentInstance: Screen? = null
            private set
        var currentUI: CrashUI? = null
            private set

        private val fallbackLang: Map<String, String> by lazy {
            runCatching {
                CrashUI::class.java.getResourceAsStream("/assets/crashpatch/lang/en_us.json")!!.use { stream ->
                    JsonParser.parseReader(stream.reader()).asJsonObject.entrySet()
                        .associate { (key, value) -> key to value.asString }
                }
            }.getOrDefault(emptyMap())
        }
    }

    private val crashScan: CrashScan? by lazy {
        val suppressed = LogScanner.reportLines()
        val extra = if (suppressed.isEmpty()) emptyMap() else mapOf(translate("crashpatch.solution.suppressed") to suppressed)

        CrashScanner.scan(scanText, type == GuiType.DISCONNECT, extra)
            ?.takeIf { it.solutions.isNotEmpty() }
    }

    var shouldCrash = false

    fun create(): Screen {
        currentUI = this
        currentInstance = this
        return this
    }

    override val blurBackground: Boolean get() = true

    override val closeOnEscape: Boolean get() = type != GuiType.INIT

    //~ if = 1.8.9 'client.level' -> 'client.world'
    override fun blurAlpha(): Float = if (client.level == null && !OneConfigPrefs.reduceTransparency) 1f else super.blurAlpha()

    override fun onClosed() {
        leaveWorldCrash = false
        currentInstance = null
        currentUI = null
    }

    @Composable
    override fun Content() {
        var selectedSolution by remember(crashScan) {
            mutableStateOf(crashScan?.solutions?.firstOrNull())
        }
        val status = rememberStatusMessage(durationMillis = 0L)
        val tabScroll = rememberScrollState()

        val current = OneConfigTheme.current
        val typography = OneConfigTheme.typography
        val spacing = current.spacing
        CenteredPanel(width = 650.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    "warning",
                    null,
                    modifier = Modifier.size(32.dp),
                    tint = current.textColor,
                )
                Text(
                    text = translate(type.title),
                    color = current.textColor,
                    style = typography.title,
                )
                Spacer(Modifier.height(spacing.l))
                Text(
                    text = translate("${type.title}.desc.1"),
                    color = current.textColorSecondary,
                    style = typography.body,
                )
                if (type != GuiType.INIT || crashScan != null) {
                    Text(
                        text = translate("${type.title}.desc.2"),
                        color = current.textColorSecondary,
                        style = typography.body,
                    )
                }

                Spacer(Modifier.height(spacing.xxl))
                Text(
                    text = translate(if (type == GuiType.DISCONNECT) "crashpatch.disconnect.cause" else "crashpatch.crash.cause"),
                    color = current.textColorSecondary,
                    style = typography.heading,
                )
                Spacer(Modifier.height(spacing.m))
                Text(
                    text = susThing,
                    color = OneConfigTheme.accent.legibleOn(current.pageSurface),
                    style = typography.title,
                )

                val solutions = crashScan?.solutions.orEmpty()
                if (solutions.isNotEmpty()) {
                    val activeSolution = selectedSolution ?: solutions.first()

                    Spacer(Modifier.height(spacing.xxl))
                    CodeBlock(
                        text = activeSolution.solutions.joinToString("\n"),
                        height = 220.dp,
                        wrap = true,
                        header = {
                            ScrollContainer(tabScroll, Modifier.weight(1f), orientation = Orientation.Horizontal, scrollbar = false) {
                                TabRow(
                                    tabs = solutions.map { TabItem(it, it.name) },
                                    selected = activeSolution,
                                    onSelect = { selectedSolution = it },
                                    style = TabStyle.Underline,
                                    size = ControlSize.Compact,
                                    fill = false,
                                )
                            }
                        },
                        actions = {
                            IconButton("copy", translate("crashpatch.copy"), onClick = {
                                ClipboardHelper.setString(activeSolution.solutions.joinToString("\n"))
                                status.show(translate("crashpatch.status.copied"), NotificationType.SUCCESS)
                            })
                            IconButton("upload", translate("crashpatch.upload"), onClick = {
                                val body = buildString {
                                    append(activeSolution.solutions.joinToString("\n"))
                                    append("\n\n")
                                    if (!activeSolution.isCrashReport) append(scanText)
                                }
                                val link = LogUploader.upload(body)
                                if (link == null) {
                                    status.show(translate("crashpatch.status.upload_failed"), NotificationType.ERROR)
                                } else {
                                    ClipboardHelper.setString(link)
                                    val opened = DesktopHelper.browse(URI.create(link))
                                    if (opened) {
                                        status.show(translate("crashpatch.status.uploaded"), NotificationType.SUCCESS)
                                    } else {
                                        status.show(translate("crashpatch.status.upload_copied"), NotificationType.WARNING)
                                    }
                                }
                            })
                        },
                    )
                }

                Spacer(Modifier.height(spacing.xxl))
                Text(
                    text = translate("crashpatch.discord.prompt"),
                    color = current.textColor,
                    style = typography.body,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.m),
                ) {
                    val discord = translate("crashpatch.link.discord.polyfrost")
                    val openDiscord = {
                        val opened = DesktopHelper.browse(URI.create(discord))
                        if (!opened) status.show(translate("crashpatch.status.discord_failed"), NotificationType.ERROR)
                    }
                    val discordHover = remember { MutableInteractionSource() }
                    Icon(
                        "/assets/crashpatch/discord.svg",
                        null,
                        modifier = Modifier
                            .size(28.dp)
                            .focusProperties { canFocus = false }
                            .oneConfigClickable(null, openDiscord, role = A11yRole.Link, indication = false, interactionSource = discordHover),
                        tint = current.textColor,
                    )
                    LinkText(discord, onClick = openDiscord, interactionSource = discordHover, style = typography.body)
                }

                if (status.text != null) {
                    Spacer(Modifier.height(spacing.l))
                    StatusText(status)
                }

                Spacer(Modifier.height(spacing.xxl))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing.l)
                ) {
                    val continueLabel = if (type == GuiType.INIT) "crashpatch.exit" else "crashpatch.continue"
                    TextButton(translate(continueLabel), onClick = {
                        if (type == GuiType.INIT) {
                            shouldCrash = true
                        } else {
                            dismiss()
                        }
                    }, style = ControlStyle.Primary)
                    TextButton(
                        translate("crashpatch.log"),
                        onClick = {
                            val target = file ?: return@TextButton
                            val opened = DesktopHelper.open(target)
                            if (!opened) status.show(translate("crashpatch.status.log_failed"), NotificationType.ERROR)
                        },
                        enabled = file != null,
                        trailing = {
                            Icon(
                                "external-link",
                                null,
                                Modifier.size(ButtonDefaults.iconSize(ControlSize.Regular)),
                                ButtonDefaults.contentColor(ControlStyle.Secondary),
                            )
                        },
                    )
                }
            }
        }
    }

    private fun translate(key: String): String {
        //~ if = 1.8.9 'I18n.get(' -> 'I18n.translate('
        val translated = I18n.get(key)
        return if (translated == key) fallbackLang[key] ?: key else translated
    }

    enum class GuiType(val title: String) {
        INIT("crashpatch.init"), NORMAL("crashpatch.crash"), DISCONNECT("crashpatch.disconnect")
    }
}