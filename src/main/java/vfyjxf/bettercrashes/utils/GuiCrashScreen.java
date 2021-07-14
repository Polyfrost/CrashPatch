package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;

@SideOnly(Side.CLIENT)
public class GuiCrashScreen extends GuiProblemScreen{
    public GuiCrashScreen(CrashReport report) {
        super(report);
    }
    @Override
    public void initGui() {
        super.initGui();
        GuiOptionButton mainMenuButton = new GuiOptionButton(0, width / 2 - 155, height / 4 + 120 + 12, I18n.format("bettercrashes.gui.toTitle"));
        buttonList.add(mainMenuButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        super.actionPerformed(button);
        if (button.id == 0) {
            mc.displayGuiScreen(new GuiMainMenu());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) { // TODO: localize number of lines
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("bettercrashes.crashscreen.title"), width / 2, height / 4 - 40, 0xFFFFFF);

        int textColor = 0xD0D0D0;
        int x = width / 2 - 155;
        int y = height / 4;

        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.summary"), x, y, textColor);
        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.paragraph1.line1"), x, y += 18, textColor);

        drawCenteredString(fontRendererObj, getModListString(), width / 2, y += 11, 0xE0E000);

        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.paragraph2.line1"), x, y += 11, textColor);

        drawCenteredString(fontRendererObj, report.getFile() != null ? "\u00A7n" + report.getFile().getName() : I18n.format("vanillafix.crashscreen.reportSaveFailed"), width / 2, y += 11, 0x00FF00);

        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.paragraph3.line1"), x, y += 12, textColor);
        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.paragraph3.line2"), x, y += 9, textColor);
        drawString(fontRendererObj, I18n.format("bettercrashes.crashscreen.paragraph3.line3"), x, y += 9, textColor);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
