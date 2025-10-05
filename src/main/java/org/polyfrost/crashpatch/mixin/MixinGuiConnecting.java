package org.polyfrost.crashpatch.mixin;

import dev.deftu.omnicore.api.client.OmniDesktop;
import net.minecraft.util.EnumChatFormatting;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import org.polyfrost.crashpatch.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Mixin(GuiConnecting.class)
public class MixinGuiConnecting extends GuiScreen {

    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void drawWarningText(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (((MinecraftHook) Minecraft.getMinecraft()).hasRecoveredFromCrash()) {
            crashpatch$drawSplitCenteredString(crashpatch$getText(), width / 2, 5, Color.WHITE.getRGB());
        }
    }

    @Unique
    private String crashpatch$getText() {
        return EnumChatFormatting.RED + "If Minecraft is stuck on this screen, please force close the game" + (CrashPatchClient.INSTANCE.isSkyclient() ? " and go to https://discord.gg/eh7tNFezct for support" : "") + ".";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
            //#if FORGE
            throws IOException
            //#endif
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (((MinecraftHook) Minecraft.getMinecraft()).hasRecoveredFromCrash()) {
            if (mouseButton == 0) {
                List<String> list = this.fontRendererObj.listFormattedStringToWidth(crashpatch$getText(), width);
                int width = -1;
                for (String text : list) {
                    width = Math.max(width, this.fontRendererObj.getStringWidth(text));
                }

                int left = (this.width / 2) - width / 2;
                if ((width == -1 || (left < mouseX && left + width > mouseX)) && (mouseY > 5 && mouseY < 15 + ((list.size() - 1) * (this.fontRendererObj.FONT_HEIGHT + 2)))) {
                    OmniDesktop.browse(URI.create("https://discord.gg/eh7tNFezct"));
                }
            }
        }
    }

    @Unique
    public void crashpatch$drawSplitCenteredString(String text, int x, int y, int color) {
        for (String line : this.fontRendererObj.listFormattedStringToWidth(text, width)) {
            drawCenteredString(this.fontRendererObj, line, x, y, color);
            y += this.fontRendererObj.FONT_HEIGHT + 2;
        }
    }
}
