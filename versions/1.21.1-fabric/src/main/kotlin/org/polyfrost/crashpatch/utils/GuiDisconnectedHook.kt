package org.polyfrost.crashpatch.utils

import net.minecraft.client.gui.screen.DisconnectedScreen
import net.minecraft.client.gui.screen.Screen
import org.polyfrost.crashpatch.crashes.CrashScanStorage.scanReport
import org.polyfrost.crashpatch.mixin.AccessorGuiDisconnected
import org.polyfrost.crashpatch.CrashPatchConfig
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.polyfrost.crashpatch.gui.CrashUI
import org.polyfrost.oneconfig.utils.v1.dsl.mc

object GuiDisconnectedHook {

    @JvmStatic
    fun onGUIDisplay(screen: Screen?, ci: CallbackInfo) {
        if (screen is DisconnectedScreen && CrashPatchConfig.disconnectCrashPatch) {
            val reason = (screen as AccessorGuiDisconnected).details.comp_2853.string

            val scan = scanReport(reason, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                mc.setScreen(CrashUI(reason, null, reason, CrashUI.GuiType.DISCONNECT).create())
            }
        }
    }

}