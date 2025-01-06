package org.polyfrost.crashpatch.utils

import org.polyfrost.crashpatch.crashes.CrashHelper.scanReport
import org.polyfrost.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import cc.polyfrost.oneconfig.utils.dsl.mc
import org.polyfrost.crashpatch.config.CrashPatchConfig
import org.polyfrost.crashpatch.gui.CrashGui

object GuiDisconnectedHook {
    fun onGUIDisplay(i: GuiScreen?, ci: CallbackInfo) {
        if (i is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val gui = i as AccessorGuiDisconnected
            val scan = scanReport(gui.message.formattedText, true)
            if (scan != null && scan.solutions.size > 1) {
                try {
                    mc.displayGuiScreen(CrashGui(gui.message.formattedText, null, gui.reason, CrashGui.GuiType.DISCONNECT))
                    ci.cancel()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }
}