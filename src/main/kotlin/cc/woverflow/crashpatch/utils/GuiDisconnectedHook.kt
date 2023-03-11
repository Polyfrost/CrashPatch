package cc.woverflow.crashpatch.utils

import cc.woverflow.crashpatch.crashes.CrashHelper.scanReport
import cc.woverflow.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import cc.polyfrost.oneconfig.utils.dsl.mc
import cc.woverflow.crashpatch.config.CrashPatchConfig
import cc.woverflow.crashpatch.gui.CrashGui

object GuiDisconnectedHook {
    fun onGUIDisplay(i: GuiScreen?, ci: CallbackInfo) {
        if (i is GuiDisconnected && CrashPatchConfig.disconnectCrashPatch) {
            val gui = i as AccessorGuiDisconnected
            val scan = scanReport(gui.message.formattedText, true)
            if (scan != null && scan.solutions.size > 1) {
                ci.cancel()
                mc.displayGuiScreen(CrashGui(gui.message.formattedText, null, gui.reason, CrashGui.GuiType.DISCONNECT))
            }
        }
    }
}