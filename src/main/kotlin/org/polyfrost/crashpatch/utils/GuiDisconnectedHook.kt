package org.polyfrost.crashpatch.utils

import org.polyfrost.crashpatch.crashes.CrashScanStorage.scanReport
import org.polyfrost.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.polyfrost.crashpatch.CrashPatchConfig
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.polyfrost.crashpatch.gui.CrashUI
import org.polyfrost.oneconfig.utils.v1.dsl.mc

object GuiDisconnectedHook {

    @JvmStatic
    fun onGUIDisplay(screen: GuiScreen?, ci: CallbackInfo) {
        if (screen is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val reason = (screen as AccessorGuiDisconnected).reason
                //#if MC>=1.16
                //$$ .string
                //#endif

            val scan = scanReport(reason, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                mc.displayGuiScreen(CrashUI(reason, null, reason, CrashUI.GuiType.DISCONNECT).create())
            }
        }
    }

}