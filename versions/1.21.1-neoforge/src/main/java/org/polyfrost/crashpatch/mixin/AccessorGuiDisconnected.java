package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.DisconnectionDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisconnectedScreen.class)
public interface AccessorGuiDisconnected {
    @Accessor
    DisconnectionDetails getDetails();
}
