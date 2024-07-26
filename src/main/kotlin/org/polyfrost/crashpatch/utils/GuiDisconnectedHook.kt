package org.polyfrost.crashpatch.utils

import org.polyfrost.crashpatch.crashes.CrashHelper.scanReport
import org.polyfrost.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import org.polyfrost.crashpatch.config.CrashPatchConfig
import org.polyfrost.crashpatch.gui.CrashGuiRewrite
import org.polyfrost.crashpatch.mc

object GuiDisconnectedHook {
    fun onGUIDisplay(i: GuiScreen?, ci: CallbackInfo) {
        if (i is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val gui = i as AccessorGuiDisconnected
            val scan = scanReport(gui.message.formattedText, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                mc.displayGuiScreen(CrashGuiRewrite(gui.message.formattedText, null, gui.reason, CrashGuiRewrite.GuiType.DISCONNECT).create())
            }
        }
    }
}