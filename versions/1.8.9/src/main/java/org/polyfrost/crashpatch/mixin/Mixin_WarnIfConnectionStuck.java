package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Formatting;
import org.polyfrost.crashpatch.CrashPatchConstants;
import org.polyfrost.oneconfig.api.platform.v1.DesktopHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.net.URI;
import java.util.List;

@Mixin(ConnectScreen.class)
public class Mixin_WarnIfConnectionStuck extends Screen {
    @Inject(method = "render", at = @At("TAIL"))
    private void drawWarningText(int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        if (CrashPatchConstants.recoveredFromCrash) {
            crashpatch$drawSplitCenteredString(crashpatch$getText(), width / 2, 5, Color.WHITE.getRGB());
        }
    }

    @Unique
    private String crashpatch$getText() {
        return Formatting.RED + "If Minecraft is stuck on this screen, please force close the game and go to https://discord.gg/polyfrost for support.";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (CrashPatchConstants.recoveredFromCrash) {
            if (button == 0) {
                List<String> list = this.textRenderer.split(crashpatch$getText(), width);
                int width = -1;
                for (String text : list) {
                    width = Math.max(width, this.textRenderer.getWidth(text));
                }

                int left = (this.width / 2) - width / 2;
                if ((width == -1 || (left < mouseX && left + width > mouseX)) && (mouseY > 5 && mouseY < 15 + ((list.size() - 1) * (this.textRenderer.fontHeight + 2)))) {
                    DesktopHelper.browse(URI.create("https://discord.gg/polyfrost"));
                }
            }
        }
    }

    @Unique
    public void crashpatch$drawSplitCenteredString(String text, int x, int y, int color) {
        for (String line : this.textRenderer.split(text, width)) {
            drawCenteredString(this.textRenderer, line, x, y, color);
            y += this.textRenderer.fontHeight + 2;
        }
    }
}
