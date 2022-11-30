package cc.woverflow.crashpatch.utils

import cc.woverflow.crashpatch.crashes.CrashHelper.scanReport
import cc.woverflow.crashpatch.mixin.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.gui.GuiScreen
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiDisconnectedHook {
    fun onGUIDisplay(i: GuiScreen?, ci: CallbackInfo) {
        if (i is GuiDisconnected) {
            val gui = i as AccessorGuiDisconnected
            val scan = scanReport(gui.message.formattedText, true)
            if (scan != null && scan.solutions.isNotEmpty()) {
                ci.cancel()

                //todo
                //displayGuiScreen(new GuiServerDisconnectMenu(gui.getMessage(), gui.getReason(), scan));
            }
        }
    }
}