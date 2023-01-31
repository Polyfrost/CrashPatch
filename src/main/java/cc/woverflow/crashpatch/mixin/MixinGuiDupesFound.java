package cc.woverflow.crashpatch.mixin;

import cc.polyfrost.oneconfig.libs.universal.UDesktop;
import cc.woverflow.crashpatch.CrashPatchKt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.GuiDupesFound;
import net.minecraftforge.fml.common.DuplicateModsFoundException;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.File;
import java.util.Map;

@Mixin(GuiDupesFound.class)
public class MixinGuiDupesFound extends GuiErrorScreen {

    @Shadow
    private DuplicateModsFoundException dupes;

    public MixinGuiDupesFound() {
        super(null, null);
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.buttonList.add(new GuiButton(0, width / 2 - 100, height - 50, "Open Folder"));
        this.buttonList.add(new GuiButton(1, width / 2 - 100, height - 30, "Quit Game"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                UDesktop.open(new File(CrashPatchKt.getMcDir(), "mods"));
                break;
            case 1:
                FMLCommonHandler.instance().exitJava(0, false);
                break;
        }
    }

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ci.cancel();
        drawDefaultBackground();
        int offset = 10;
        offset += drawSplitString("There are duplicate mods in your mod folder!", width / 2, offset, width, Color.RED.getRGB());

        for (Map.Entry<ModContainer, File> modContainerFileEntry : dupes.dupes.entries()) {
            offset += 10;
            offset += drawSplitString(String.format("%s : %s", modContainerFileEntry.getKey().getModId(), modContainerFileEntry.getValue().getName()), width / 2, offset, width, Color.YELLOW.getRGB());
        }

        offset += 10;

        drawSplitString(EnumChatFormatting.BOLD + "To fix this, go into your mods folder by clicking the button below or going to " + CrashPatchKt.getMcDir().getAbsolutePath() + " and deleting the duplicate mods.", width / 2, offset, width, Color.BLUE.getRGB());

        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY);
        }
        for (net.minecraft.client.gui.GuiLabel guiLabel : this.labelList) {
            guiLabel.drawLabel(this.mc, mouseX, mouseY);
        }
    }

    private static int drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        str = trimStringNewline(str);
        int y2 = y;
        for (String s : Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(str, wrapWidth)) {
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, (float) (x - Minecraft.getMinecraft().fontRendererObj.getStringWidth(s) / 2), (float) y2, textColor);
            y2 += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        }
        return y2 - y;
    }

    private static String trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
