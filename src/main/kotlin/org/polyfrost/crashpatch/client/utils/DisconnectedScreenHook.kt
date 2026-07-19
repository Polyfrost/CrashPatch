package org.polyfrost.crashpatch.client.utils

import net.minecraft.client.gui.screens.DisconnectedScreen
import net.minecraft.client.gui.screens.Screen
import org.polyfrost.crashpatch.client.CrashPatchConfig
import org.polyfrost.crashpatch.client.crashes.CrashScanner
import org.polyfrost.crashpatch.client.gui.CrashUI
import org.polyfrost.crashpatch.mixin.Mixin_AccessDisconnectReason
import org.polyfrost.oneconfig.utils.v1.dsl.mc
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object DisconnectedScreenHook {
    @JvmStatic
    fun onScreenDisplayed(screen: Screen?, ci: CallbackInfo) {
        if (screen is DisconnectedScreen && CrashPatchConfig.disconnectCrashPatch) {
            val reason = reason(screen)
            val scan = CrashScanner.scan(reason, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                //? if < 26.2 {
                /*mc.setScreen(CrashUI(reason, null, reason, CrashUI.GuiType.DISCONNECT).create())
                *///? } else {
                mc.gui.setScreen(CrashUI(reason, null, reason, CrashUI.GuiType.DISCONNECT).create())
                //? }
            }
        }
    }

    private fun reason(screen: Screen): String {
        return (screen as Mixin_AccessDisconnectReason).info.reason.string
    }
}
