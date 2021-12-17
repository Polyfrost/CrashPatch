/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/99cb47cc05b4790e8ef02bbcac932b21dafa107f/src/main/java/org/dimdev/vanillafix/crashes/GuiProblemScreen.java
 *The source file uses the MIT License.
 */

package net.wyvest.bettercrashes.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.wyvest.bettercrashes.utils.CrashReportUpload;
import net.wyvest.bettercrashes.utils.CrashUtils;
import net.wyvest.bettercrashes.hook.CrashReportHook;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    public final CrashReport report;
    private String hasteLink = null;
    private String modListString;

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 50, height / 4 + 120 + 12, 110, 20, "Open Crash Report"));
        buttonList.add(new GuiButton(2, width / 2 - 50 + 115, height / 4 + 120 + 12, 110, 20, "Upload and copy link"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            try {
                CrashUtils.openCrashReport(report);
            } catch (IOException e) {
                button.displayString = "[Failed]";
                button.enabled = false;
                e.printStackTrace();
            }
        }
        if (button.id == 2) {
            if (hasteLink == null) {
                try {
                    hasteLink = CrashReportUpload.uploadToUbuntuPastebin("https://paste.ubuntu.com", report.getCompleteReport());
                } catch (IOException e) {
                    button.displayString = "[Failed]";
                    button.enabled = false;
                    e.printStackTrace();
                }
            }
            setClipboardString(hasteLink);
        }

    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
    }

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((CrashReportHook) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = "[Error identifying]";
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = "Unknown";
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }
}
