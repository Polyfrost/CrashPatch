package net.wyvest.crashpatch.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.crash.CrashReport
import org.apache.commons.lang3.StringUtils
import net.wyvest.crashpatch.crashes.CrashUtils
import net.wyvest.crashpatch.hooks.CrashReportHook
import java.io.IOException

abstract class GuiProblemScreen(val report: CrashReport) : GuiScreen() {
    private var hasteLink: String? = null
    protected var modListString: String? = null
        get() {
            if (field == null) {
                val suspectedMods =
                    (report as CrashReportHook).suspectedMods ?: return "[Error identifying]".also { field = it }
                val modNames: MutableList<String?> = ArrayList()
                for (mod in suspectedMods) {
                    modNames.add(mod.name)
                }
                field = if (modNames.isEmpty()) {
                    "Unknown"
                } else {
                    StringUtils.join(modNames, ", ")
                }
            }
            return field
        }
        private set

    override fun initGui() {
        mc.setIngameNotInFocus()
        buttonList.clear()
        buttonList.add(GuiButton(1, width / 2 - 50, height / 4 + 120 + 12, 110, 20, "Open Crash Report"))
        buttonList.add(GuiButton(2, width / 2 - 50 + 115, height / 4 + 120 + 12, 110, 20, "Upload and copy link"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 1) {
            try {
                CrashUtils.openCrashReport(report)
            } catch (e: IOException) {
                button.displayString = "[Failed]"
                button.enabled = false
                e.printStackTrace()
            }
        }
        if (button.id == 2) {
            if (hasteLink == null) {
                try {
                    hasteLink =
                        CrashUtils.uploadToUbuntuPastebin("https://paste.ubuntu.com", report.completeReport)
                } catch (e: IOException) {
                    button.displayString = "[Failed]"
                    button.enabled = false
                    e.printStackTrace()
                }
            }
            setClipboardString(hasteLink)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}