package net.wyvest.bettercrashes.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.wyvest.bettercrashes.hook.FontRendererHook;
import net.wyvest.bettercrashes.utils.CrashScan;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiIssuesScreen extends GuiScreen {

    private final GuiScreen parent;

    private final CrashScan crashScan;

    public GuiIssuesScreen(CrashScan crashScan, GuiScreen parent) {
        this.crashScan = crashScan;
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 90, height / 4 + 120 + 12, 180, 20, "Return to Previous Screen"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        AtomicInteger y = new AtomicInteger();
        crashScan.getResponses().forEach((title, stuff) -> {
            drawCenteredString(fontRendererObj, title, width / 2, y.addAndGet(10), Color.WHITE.getRGB());
            y.addAndGet(10);
            for (String text : stuff) {
                y.addAndGet(((FontRendererHook) fontRendererObj).bettercrashes$drawSplitString(text, 5, y.get(), width - 5, Color.WHITE.getRGB()));
                y.addAndGet(5);
            }
        });
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
