package cc.woverflow.crashpatch.mixin;

import net.minecraft.client.gui.FontRenderer;
import cc.woverflow.crashpatch.hooks.FontRendererHook;
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

    @Shadow public abstract int drawStringWithShadow(String text, float x, float y, int color);

    @Shadow public abstract int getStringWidth(String text);

    @Override
    public int drawCrashPatchSplitText(String str, int x, int y, int wrapWidth, int textColor) {
        resetStyles();
        this.textColor = textColor;
        str = trimStringNewline(str);
        int y2 = y;
        for (String s : listFormattedStringToWidth(str, wrapWidth)) {
            drawStringWithShadow(s, (float)(x - getStringWidth(s) / 2), (float)y2, textColor);
            y2 += FONT_HEIGHT;
        }
        return y2 - y;
    }
}
