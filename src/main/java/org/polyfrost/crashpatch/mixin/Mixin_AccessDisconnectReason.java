package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.screens.DisconnectedScreen;
//? if > 1.8.9 {
import net.minecraft.network.DisconnectionDetails;
//?} else
//import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DisconnectedScreen.class)
public interface Mixin_AccessDisconnectReason {
    //? if > 1.8.9 {
    @Accessor("details")
    DisconnectionDetails getInfo();
    //?} else {
    /*@Accessor("reason")
    Text getReason();
    *///?}
}
