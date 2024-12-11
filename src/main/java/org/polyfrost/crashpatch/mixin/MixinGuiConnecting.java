package org.polyfrost.crashpatch.mixin;

import org.polyfrost.universal.ChatColor;
import org.polyfrost.universal.UDesktop;
import org.polyfrost.crashpatch.CrashPatch;
import org.polyfrost.crashpatch.hooks.MinecraftHook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
        return ChatColor.RED + "If Minecraft is stuck on this screen, please force close the game" + (CrashPatch.INSTANCE.isSkyclient() ? " and go to https://discord.gg/eh7tNFezct for support" : "") + ".";
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (((MinecraftHook) Minecraft.getMinecraft()).hasRecoveredFromCrash()) {
            if (mouseButton == 0) {
                String[] list = crashpatch$wrapFormattedStringToWidth(crashpatch$getText(), width).split("\n");
                int width = -1;
                for (String text : list) {
                    width = Math.max(width, this.fontRendererObj.getStringWidth(text));
                }

                int left = (this.width / 2) - width / 2;
                if ((width == -1 || (left < mouseX && left + width > mouseX)) && (mouseY > 5 && mouseY < 15 + ((list.length - 1) * (this.fontRendererObj.FONT_HEIGHT + 2)))) {
                    UDesktop.browse(URI.create("https://discord.gg/eh7tNFezct"));
                }
            }
        }
    }

    @Unique
    public void crashpatch$drawSplitCenteredString(String text, int x, int y, int color) {
        for (String line : crashpatch$wrapFormattedStringToWidth(text, width).split("\n")) {
            drawCenteredString(this.fontRendererObj, line, x, y, color);
            y += this.fontRendererObj.FONT_HEIGHT + 2;
        }
    }

    @Unique
    public String crashpatch$wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.crashpatch$sizeStringToWidth(str, wrapWidth);

        if (str.length() <= i) {
            return str;
        } else {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == 32 || c0 == 10;
            String s1 = FontRenderer.getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.crashpatch$wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    @Unique
    private int crashpatch$sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        int j = 0;
        int k = 0;
        int l = -1;

        for (boolean flag = false; k < i; ++k) {
            char c0 = str.charAt(k);

            switch (c0) {
                case '\n':
                    --k;
                    break;
                case ' ':
                    l = k;
                default:
                    j += this.fontRendererObj.getCharWidth(c0);

                    if (flag) {
                        ++j;
                    }

                    break;
                case '\u00a7':

                    if (k < i - 1) {
                        ++k;
                        char c1 = str.charAt(k);

                        if (c1 != 108 && c1 != 76) {
                            if (c1 == 114 || c1 == 82 || crashpatch$isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == 10) {
                ++k;
                l = k;
                break;
            }

            if (j > wrapWidth) {
                break;
            }
        }

        return k != i && l != -1 && l < k ? l : k;
    }

    @Unique
    private static boolean crashpatch$isFormatColor(char colorChar) {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }
}
