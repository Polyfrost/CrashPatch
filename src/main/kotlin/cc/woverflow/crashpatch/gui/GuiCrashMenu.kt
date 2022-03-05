package cc.woverflow.crashpatch.gui

import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.gui.components.Button
import cc.woverflow.crashpatch.gui.components.TextButton
import cc.woverflow.crashpatch.hooks.CrashReportHook
import cc.woverflow.crashpatch.utils.InternetUtils
import cc.woverflow.onecore.utils.browseURL
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.ChatColor
import gg.essential.universal.UDesktop
import gg.essential.vigilance.gui.VigilancePalette
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.crash.CrashReport
import java.awt.Color
import java.io.IOException

class GuiCrashMenu @JvmOverloads constructor(val report: CrashReport, private val init: Boolean = false) : WindowScreen(version = ElementaVersion.V1) {
    private var hasteLink: String? = null
    private val crashScan by lazy {
        var yes = CrashHelper.scanReport(report.completeReport)
        if (yes != null && yes!!.solutions.isEmpty()) {
            yes = null
        }
        yes
    }
    var shouldCrash = false
    private var hasteFailed = false

    private val contentContainer by UIContainer() constrain {
        x = 0.pixels()
        y = 0.pixels()
        width = 100.percent()
        height = 90.percent()
    } childOf window

    private val content by ScrollComponent() constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf contentContainer

    private val scrollBar by Button(unfocusedScrollBar.toConstraint(), focusedScrollBar.toConstraint()) {} constrain {
        x = 7.5f.pixels(true)
        width = 3.pixels()
        color = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 128).toConstraint()
    } childOf contentContainer

    private val crashedText by UIWrappedText("${ChatColor.RED}Minecraft has crashed!", centered = true) constrain {
        x = 2.percent()
        y = 2.percent()
        width = 96.percent()
        textScale = 2.pixels()
    } childOf content

    private val first by UIWrappedText("""
        Minecraft ran into a problem and crashed.
        The following mod may have caused this crash:
    """.trimIndent(), centered = true) constrain {
        x = 2.percent()
        y = SiblingConstraint(9f)
        width = 96.percent()
    } childOf content

    private val susMods by UIWrappedText("${ChatColor.YELLOW}${(report as CrashReportHook).suspectedCrashPatchMods}", centered = true) constrain {
        x = 2.percent()
        y = SiblingConstraint(9f)
        width = 96.percent()
        textScale = (1.5).pixels()
    } childOf content

    private val second by UIWrappedText("""
        This may not be 100% accurate.
        ${if (!CrashPatch.isSkyclient) "You're encouraged to send this crash report to the mod's developers to help fix the issue." else "${ChatColor.RED}PLEASE GO TO https://discord.gg/eh7tNFezct FOR SUPPORT."}${if (init) "" else "\n${ChatColor.YELLOW}Since CrashPatch is installed, you can most likely keep on playing despite the crash."}
        ${if (crashScan != null) "You may also have a look at the suggestions below to fix the issue.\n" else ""}
    """.trimIndent(), centered = true) constrain {
        x = 2.percent()
        y = SiblingConstraint(9f)
        width = 96.percent()
    } childOf content

    init {
        second.onLeftClick {
            if (CrashPatch.isSkyclient) {
                UDesktop.browseURL("https://discord.gg/eh7tNFezct")
            }
        }
    }

    private val block by UIBlock(VigilancePalette.getLightBackground()) constrain {
        x = 2.percent()
        y = SiblingConstraint(5f)
        width = 96.percent()
        height = 50.percent()
    } childOf content

    private val scrollableSolutions by ScrollComponent("No solutions found :(", customScissorBoundingBox = block) constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf block

    private val solutionsScrollBar by Button(unfocusedScrollBar.toConstraint(), focusedScrollBar.toConstraint()) {} constrain {
        x = 7.5f.pixels(true)
        width = 3.pixels()
        color = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 128).toConstraint()
    } childOf block

    init {
        crashScan?.solutions?.let {
            var yes = 0
            it.forEach { list ->
                ++yes
                UIWrappedText(list.key, centered = true) constrain {
                    x = 0.pixels()
                    y = if (yes == 1) 5.pixels() else SiblingConstraint(6f)
                    width = 100.percent()
                    textScale = 3.pixels()
                } childOf scrollableSolutions
                for (solution in list.value) {
                    UIWrappedText(solution, centered = true) constrain {
                        x = 0.pixels()
                        y = SiblingConstraint(4f)
                        width = 100.percent()
                    } childOf scrollableSolutions
                }
            }
            UIContainer() constrain {
                x = 0.pixels()
                y = SiblingConstraint(4f)
                width = 100.percent()
                height = 10.pixels()
            } childOf scrollableSolutions
        }
        scrollableSolutions.setVerticalScrollBarComponent(solutionsScrollBar, true)
    }

    private val buttonContainer by UIContainer() constrain {
        x = 0.pixels()
        y = 90.percent()
        width = 100.percent()
        height = (window.getHeight() - 2).pixels() - 90.percent()
    } childOf window
    private val openCrashReport by TextButton("Open Crash Report", black, white) {
        UDesktop.open(report.file)
    } constrain {
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf buttonContainer
    val close by TextButton(if (init) "Quit Game" else "Return to Game", black, white) {
        if (init) {
            shouldCrash = true
        } else {
            restorePreviousScreen()
        }
    } constrain {
        x = SiblingConstraint(5f, true)
        y = CenterConstraint()
    } childOf buttonContainer
    val uploadReport by TextButton("Upload Crash Report", black, white, { !hasteFailed }) {
        setClipboardString(hasteLink ?: run {
            try {
                hasteLink = InternetUtils.uploadToHastebin(report.completeReport)
                return@run hasteLink
            } catch (e: IOException) {
                hasteFailed = true
                (this@TextButton as TextButton).setText("[Failed]")
                e.printStackTrace()
            }
            return@run null
        })
    } constrain {
        x = (openCrashReport.getRight() + 5).pixels()
        y = CenterConstraint()
    } childOf buttonContainer

    init {
        content.setVerticalScrollBarComponent(scrollBar, true)
    }

    companion object {
        private val white = Color(255, 255, 255, 200).toConstraint()
        private val black = Color(0, 0, 0, 200).toConstraint()
        private val focusedScrollBar = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 230)
        private val unfocusedScrollBar = Color(VigilancePalette.getScrollBar().red, VigilancePalette.getScrollBar().green, VigilancePalette.getScrollBar().blue, 128)
    }
}