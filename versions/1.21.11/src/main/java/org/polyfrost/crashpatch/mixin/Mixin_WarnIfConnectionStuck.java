package org.polyfrost.crashpatch.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.crashpatch.CrashPatchConstants;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
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
    protected Mixin_WarnIfConnectionStuck(Component arg) {
        super(arg);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void drawWarningText(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (CrashPatchConstants.recoveredFromCrash) {
            crashpatch$drawSplitCenteredString(guiGraphics, crashpatch$getText(), width / 2, 5, Color.WHITE.getRGB());
        }
    }

    @Unique
    private String crashpatch$getText() {
        return ChatFormatting.RED + "If Minecraft is stuck on this screen, please force close the game and go to https://discord.gg/polyfrost for support.";
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        boolean clicked = super.mouseClicked(mouseButtonEvent, bl);
        if (!clicked) {
            return true;
        }
        if (CrashPatchConstants.recoveredFromCrash) {
            if (mouseButtonEvent.button() == 0) {
                List<FormattedCharSequence> list = this.font.split(FormattedText.of(crashpatch$getText()), width);
                int width = -1;
                for (FormattedCharSequence text : list) {
                    width = Math.max(width, this.font.width(text));
                }

                int left = (this.width / 2) - width / 2;
                double mouseX = mouseButtonEvent.x();
                double mouseY = mouseButtonEvent.y();
                if ((width == -1 || (left < mouseX && left + width > mouseX)) && (mouseY > 5 && mouseY < 15 + ((list.size() - 1) * (this.font.lineHeight + 2)))) {
                    DesktopHelper.browse(URI.create("https://discord.gg/polyfrost"));
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    public void crashpatch$drawSplitCenteredString(GuiGraphics ctx, String text, int x, int y, int color) {
        for (FormattedCharSequence line : this.font.split(FormattedText.of(text), width)) {
            ctx.drawString(this.font, line, (int) (x - ((float) this.font.width(line) / 2)), y, color);
            y += this.font.lineHeight + 2;
        }
    }
}
