package org.polyfrost.crashpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.deftu.omnicore.api.client.OmniDesktop;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.polyfrost.crashpatch.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
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
    private void drawWarningText(
            //#if MC<1.20
            PoseStack arg,
            //#else
            //$$ net.minecraft.client.gui.DrawContext arg,
            //#endif
            int i, int j, float f, CallbackInfo ci) {
        if (((MinecraftHook) Minecraft.getInstance()).hasRecoveredFromCrash()) {
            crashpatch$drawSplitCenteredString(arg, crashpatch$getText(), width / 2, 5, Color.WHITE.getRGB());
        }
    }

    @Unique
    private String crashpatch$getText() {
        return ChatFormatting.RED + "If Minecraft is stuck on this screen, please force close the game" + (CrashPatchClient.isSkyClient() ? " and go to https://discord.gg/eh7tNFezct for support" : "") + ".";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        boolean clicked = super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!clicked) {
            return true;
        }
        if (((MinecraftHook) Minecraft.getInstance()).hasRecoveredFromCrash()) {
            if (mouseButton == 0) {
                List<FormattedCharSequence> list = this.font.split(FormattedText.of(crashpatch$getText()), width);
                int width = -1;
                for (FormattedCharSequence text : list) {
                    width = Math.max(width, this.font.width(text));
                }

                int left = (this.width / 2) - width / 2;
                if ((width == -1 || (left < mouseX && left + width > mouseX)) && (mouseY > 5 && mouseY < 15 + ((list.size() - 1) * (this.font.lineHeight + 2)))) {
                    OmniDesktop.browse(URI.create("https://discord.gg/eh7tNFezct"));
                    return true;
                }
            }
        }
        return false;
    }

    @Unique
    public void crashpatch$drawSplitCenteredString(
                                                   //#if MC<1.20
                                                   PoseStack stack,
                                                   //#else
                                                   //$$ net.minecraft.client.gui.DrawContext ctx,
                                                   //#endif
                                                   String text, int x, int y, int color) {
        for (FormattedCharSequence line : this.font.split(FormattedText.of(text), width)) {
            //#if MC<1.20
            this.font.drawShadow(stack,
            //#else
            //$$ ctx.drawTextWithShadow(this.textRenderer,
            //#endif
                    line, (int) (x - ((float) this.font.width(line) / 2)), y, color);
            y += this.font.lineHeight + 2;
        }
    }
}
