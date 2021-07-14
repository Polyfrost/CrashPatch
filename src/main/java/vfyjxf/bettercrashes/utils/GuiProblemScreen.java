package vfyjxf.bettercrashes.utils;

import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import org.apache.commons.lang3.StringUtils;
import vfyjxf.bettercrashes.BetterCrashes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SideOnly(Side.CLIENT)
public abstract class GuiProblemScreen extends GuiScreen {

    protected final CrashReport report;
    private String hasteLink = null;
    private String modListString;

    public GuiProblemScreen(CrashReport report) {
        this.report = report;
    }

    @Override
    public void initGui() {
        mc.setIngameNotInFocus();
        buttonList.clear();
        buttonList.add(new GuiButton(1, width / 2 - 155 + 160, height / 4 + 120 + 12, 150, 20, I18n.format("bettercrashes.gui.openCrashReport")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if(button.id == 1){
            try {
                CrashUtils.openCrashReport(report);
            } catch (IOException e) {
                BetterCrashes.logger.error("Can't open crashreoprt",e);
                button.displayString = I18n.format("bettercrashes.gui.failed");
                button.enabled = false;
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}

    protected String getModListString() {
        if (modListString == null) {
            final Set<ModContainer> suspectedMods = ((IPatchedCrashReport) report).getSuspectedMods();
            if (suspectedMods == null) {
                return modListString = I18n.format("bettercrashes.crashscreen.identificationErrored");
            }
            List<String> modNames = new ArrayList<>();
            for (ModContainer mod : suspectedMods) {
                modNames.add(mod.getName());
            }
            if (modNames.isEmpty()) {
                modListString = I18n.format("bettercrashes.crashscreen.unknownCause");
            } else {
                modListString = StringUtils.join(modNames, ", ");
            }
        }
        return modListString;
    }
}
