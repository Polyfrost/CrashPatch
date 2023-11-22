package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface AccessorGuiDisconnected {
    @Accessor("message")
    IChatComponent getMessage();

    @Accessor("reason")
    String getReason();
}
