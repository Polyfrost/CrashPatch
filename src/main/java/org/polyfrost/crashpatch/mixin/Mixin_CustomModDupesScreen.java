package org.polyfrost.crashpatch.mixin;

//#if FORGE && MC < 1.13
import dev.deftu.omnicore.api.OmniPaths;
import dev.deftu.omnicore.api.client.OmniClient;
import dev.deftu.omnicore.api.client.OmniDesktop;
import dev.deftu.omnicore.api.client.render.OmniTextRenderer;
import dev.deftu.textile.minecraft.FormattingCodes;
import net.minecraft.client.gui.FontRenderer;
import org.polyfrost.crashpatch.client.CrashPatchClient;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.GuiDupesFound;
import net.minecraftforge.fml.common.DuplicateModsFoundException;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.io.File;
import java.util.Map;

@Mixin(GuiDupesFound.class)
public class Mixin_CustomModDupesScreen extends GuiErrorScreen {
    @Shadow private DuplicateModsFoundException dupes;

    public Mixin_CustomModDupesScreen() {
        super(null, null);
    }

    //#if MC < 1.12
    @Inject(method = "initGui", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
    //#else
    //$$ @Override
    //$$ public void initGui() {
    //$$ super.initGui();
    //#endif
        this.buttonList.add(new GuiButton(0, width / 2 - 100, height - 50, "Open Folder"));
        this.buttonList.add(new GuiButton(1, width / 2 - 100, height - 30, "Quit Game"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                OmniDesktop.open(OmniPaths.getGameDirectory().resolve("mods").toFile());
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
        offset += crashpatch$drawSplitString("There are duplicate mods in your mod folder!", width / 2, offset, width, Color.RED.getRGB());
        for (Map.Entry<ModContainer, File> modContainerFileEntry : dupes.dupes.entries()) {
            offset += 10;
            offset += crashpatch$drawSplitString(String.format("%s : %s", modContainerFileEntry.getKey().getModId(), modContainerFileEntry.getValue().getName()), width / 2, offset, width, Color.YELLOW.getRGB());
        }

        offset += 10;

        crashpatch$drawSplitString(FormattingCodes.BOLD + "To fix this, go into your mods folder by clicking the button below or going to " + OmniPaths.getGameDirectory().toAbsolutePath() + " and deleting the duplicate mods.", width / 2, offset, width, Color.BLUE.getRGB());

        for (GuiButton guiButton : this.buttonList) {
            guiButton.drawButton(this.mc, mouseX, mouseY
                    //#if MC >= 1.12
                    //$$ , partialTicks
                    //#endif
                    );
        }
        for (net.minecraft.client.gui.GuiLabel guiLabel : this.labelList) {
            guiLabel.drawLabel(this.mc, mouseX, mouseY);
        }
    }

    @Unique
    private static int crashpatch$drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        str = crashpatch$trimStringNewline(str);
        int y2 = y;
        FontRenderer fontRenderer =
                //#if MC >= 1.12.2
                //$$ OmniClient.get().fontRenderer;
                //#else
                OmniClient.get().fontRendererObj;
                //#endif
        for (String s : fontRenderer.listFormattedStringToWidth(str, wrapWidth)) {
            fontRenderer.drawStringWithShadow(s, (float) (x - OmniTextRenderer.width(s) / 2), (float) y2, textColor);
            y2 += OmniTextRenderer.getLineHeight();
        }
        return y2 - y;
    }

    @Unique
    private static String crashpatch$trimStringNewline(String text) {
        while (text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
//#endif
