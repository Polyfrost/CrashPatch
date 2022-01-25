package cc.woverflow.crashpatch.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.crash.CrashReport
import cc.woverflow.crashpatch.crashes.CrashHelper
import cc.woverflow.crashpatch.crashes.CrashScan
import net.minecraft.util.EnumChatFormatting

class GuiCrashScreen(report: CrashReport) : GuiProblemScreen(report) {
    private var crashScan: CrashScan? = null
    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(0, width / 2 - 50 - 115, height / 4 + 120 + 12, 110, 20, "Return to Main Menu"))
        crashScan = CrashHelper.scanReport(report.completeReport)
        if (crashScan != null && crashScan!!.solutions.isEmpty()) {
            crashScan = null
        }
        if (crashScan != null) {
            buttonList.add(GuiButton(44, width / 2 - 50, height / 4 + 110, 110, 20, "Show Solutions"))
        }
    }

    override fun actionPerformed(button: GuiButton) {
        super.actionPerformed(button)
        if (button.id == 0) {
            mc.displayGuiScreen(GuiMainMenu())
        } else if (button.id == 44) {
            mc.displayGuiScreen(GuiIssuesScreen(crashScan!!, this))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawCenteredString(fontRendererObj, "Minecraft crashed!", width / 2, height / 4 - 40, 0xFFFFFF)
        val textColor = 0xD0D0D0
        val x = width / 2
        var y = height / 4
        drawCenteredString(fontRendererObj, "Minecraft ran into a problem and crashed.", x, y, textColor)
        drawCenteredString(fontRendererObj, "The following mod(s) have been identified as potential causes:", x, run { y += 18; y }, textColor)
        drawCenteredString(fontRendererObj, modListString, x, run { y += 11; y }, 0xE0E000)
        drawCenteredString(fontRendererObj, "A report has been generated, click the button below to open:", x, run { y += 11; y }, textColor)
        drawCenteredString(
            fontRendererObj,
            if (report.file != null) "${EnumChatFormatting.UNDERLINE}${report.file.name}" else "Failed",
            x,
            run { y += 11; y },
            0x00FF00
        )
        drawCenteredString(
            fontRendererObj,
            "You're encouraged to send this report's link to the mod's author to help",
            x,
            run { y += 12; y },
            textColor
        )
        drawCenteredString(
            fontRendererObj,
            "them fix the issue, click the \"Upload and Copy link\" can upload report",
            x,
            run { y += 9; y },
            textColor
        )
        drawCenteredString(
            fontRendererObj,
            "and copy its link to clipboard. Since CrashPatch is installed, you",
            x,
            run { y += 9; y },
            textColor
        )
        drawCenteredString(fontRendererObj, "can keep playing despite the crash.", x, run { y += 9; y }, textColor)
        if (crashScan != null) drawString(
            fontRendererObj,
            "You should also try checking the solutions below before relaunching.",
            x,
            y + 9,
            textColor
        )
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}
