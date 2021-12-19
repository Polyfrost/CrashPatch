package net.wyvest.crashpatch.gui

import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.wyvest.crashpatch.crashes.CrashScan
import net.wyvest.crashpatch.hooks.FontRendererHook
import java.awt.Color

class GuiIssuesScreen(private val crashScan: CrashScan, private val parent: GuiScreen) : GuiScreen() {
    override fun initGui() {
        buttonList.clear()
        buttonList.add(GuiButton(1, width / 2 - 90, height / 4 + 120 + 12, 180, 20, "Return to Previous Screen"))
    }

    override fun actionPerformed(button: GuiButton) {
        if (button.id == 1) {
            mc.displayGuiScreen(parent)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        var y = 0
        crashScan.responses.forEach { (title, stuff) ->
            drawCenteredString(fontRendererObj, title, width / 2, 10.also { y += it }, Color.WHITE.rgb)
            y += 10
            for (text in stuff) {
                y += (fontRendererObj as FontRendererHook).drawCrashPatchSplitText(
                    text,
                    5,
                    y,
                    width - 5,
                    Color.WHITE.rgb
                )
                y += 5
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}
