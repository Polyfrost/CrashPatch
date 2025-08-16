package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.GuiDisconnected;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiDisconnected.class)
public interface AccessorGuiDisconnected {
    @Accessor("reason")
    //#if MC<=1.12.2
    String
    //#else
    //$$ net.minecraft.network.chat.Component
    //#endif
    getReason();
}
