package net.wyvest.bettercrashes.mixin;

import net.minecraft.client.gui.FontRenderer;
import net.wyvest.bettercrashes.hook.FontRendererHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer implements FontRendererHook {
    @Shadow
    public int FONT_HEIGHT;
    @Shadow
    private int textColor;

    @Shadow
    protected abstract void resetStyles();

    @Shadow
    protected abstract String trimStringNewline(String text);

    @Shadow
    public abstract List<String> listFormattedStringToWidth(String str, int wrapWidth);

    @Shadow
    protected abstract int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow);

    @Override
    public int bettercrashes$drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        resetStyles();
        this.textColor = textColor;
        str = trimStringNewline(str);
        int y2 = y;
        for (String s : listFormattedStringToWidth(str, wrapWidth)) {
            this.renderStringAligned(s, x, y2, wrapWidth, this.textColor, false);
            y2 += FONT_HEIGHT;
        }
        return y2 - y;
    }
}
