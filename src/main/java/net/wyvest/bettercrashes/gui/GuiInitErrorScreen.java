/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiInitErrorScreen.java
 *The source file uses the MIT License.
 */


package net.wyvest.bettercrashes.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.wyvest.bettercrashes.utils.CrashHelper;
import net.wyvest.bettercrashes.utils.CrashScan;

@SideOnly(Side.CLIENT)
public class GuiInitErrorScreen extends GuiProblemScreen {

    public CrashScan crashScan;

    public boolean shouldCrash = false;

    public GuiInitErrorScreen(CrashReport report) {
        super(report);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, width / 2 - 50 - 115, height / 4 + 120 + 12, 110, 20, "Quit Game"));
        crashScan = CrashHelper.scanReport(report.getCompleteReport());
        if (crashScan != null && crashScan.getWarnings().isEmpty() && crashScan.getRecommendations().isEmpty() && crashScan.getSolutions().isEmpty()) {
            crashScan = null;
        }
        if (crashScan != null) {
            buttonList.add(new GuiButton(44, width / 2 - 50, height / 4 + 110, 110, 20, "Show Solutions"));
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 0) {
            shouldCrash = true;
        } else if (button.id == 44) {
            mc.displayGuiScreen(new GuiIssuesScreen(crashScan, this));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Minecraft failed to start!", width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRendererObj, "An error during startup prevented Minecraft from starting.", x, y, textColor);
        drawString(fontRendererObj, "The following mod(s) have been identified as potential causes:", x, y += 18, textColor);

        drawCenteredString(fontRendererObj, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRendererObj, "A report has been generated, click the button below to open:", x, y += 11, textColor);

        drawCenteredString(fontRendererObj, report.getFile() != null ? "\u00A7n" + report.getFile().getName() : "Failed", width / 2, y += 11, 0x00FF00);


        drawString(fontRendererObj, "You're encouraged to send this report's link to the mod's author to help", x, y += 12, textColor);
        drawString(fontRendererObj, "them fix the issue, click the \"Upload and Copy link\" can upload report", x, y += 9, textColor);
        drawString(fontRendererObj, "and copy its link to clipboard. ", x, y += 9, textColor);
        if (crashScan != null)
            drawString(fontRendererObj, "You can also try checking the solutions below.", x, y + 9, textColor);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
