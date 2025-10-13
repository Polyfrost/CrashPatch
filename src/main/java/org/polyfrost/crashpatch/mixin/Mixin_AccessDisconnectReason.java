package org.polyfrost.crashpatch.mixin;

import net.minecraft.client.gui.GuiDisconnected;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC >= 1.21.1
//$$ import net.minecraft.network.DisconnectionInfo;
//#elseif MC >= 1.16.5
//$$ import net.minecraft.network.chat.Component;
//#endif

@Mixin(GuiDisconnected.class)
public interface Mixin_AccessDisconnectReason {
    //#if MC >= 1.21.1
    //$$ @Accessor("info") DisconnectionInfo getInfo();
    //#elseif MC >= 1.16.5
    //$$ @Accessor("reason") Component getReason();
    //#else
    @Accessor("reason") String getReason();
    //#endif
}
