package org.polyfrost.crashpatch.client.utils

import dev.deftu.omnicore.api.client.screen.currentScreen
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.polyfrost.crashpatch.client.CrashPatchConfig
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.crashpatch.client.gui.CrashUI
import org.polyfrost.crashpatch.mixin.Mixin_AccessDisconnectReason
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object DisconnectedScreenHook {
    @JvmStatic
    fun onScreenDisplayed(screen: GuiScreen?, ci: CallbackInfo) {
        if (screen is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val reason = reason(screen)
            val scan = CrashScanner.scan(reason, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                currentScreen = CrashUI(reason, null, reason, CrashUI.GuiType.DISCONNECT).create()
            }
        }
    }

    private fun reason(screen: GuiScreen): String {
        //#if MC >= 1.21.1
        //$$ return (screen as Mixin_AccessDisconnectReason).info.comp_2853.string
        //#elseif MC >= 1.16.5
        //$$ return (screen as Mixin_AccessDisconnectReason).reason.string
        //#else
        return (screen as Mixin_AccessDisconnectReason).reason
        //#endif
    }
}
