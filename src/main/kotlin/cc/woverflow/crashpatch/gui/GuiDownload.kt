package cc.woverflow.crashpatch.gui

import gg.essential.api.EssentialAPI
import gg.essential.api.gui.buildConfirmationModal
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.dsl.childOf
import cc.woverflow.crashpatch.CrashPatch
import cc.woverflow.crashpatch.utils.Updater
import gg.essential.elementa.ElementaVersion
import java.io.File

class GuiDownload : WindowScreen(version = ElementaVersion.V1, restoreCurrentGuiOnClose = true) {

    /**
     * Initializes the screen and builds an Essential Confirmation Modal.
     */
    override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)
        EssentialAPI.getEssentialComponentFactory().buildConfirmationModal {
            this.text = "Are you sure you want to update?"
            this.secondaryText = "(This will update from v${CrashPatch.VERSION} to ${Updater.latestTag})"
            this.onConfirm = {
                restorePreviousScreen()
                gg.essential.api.utils.Multithreading.runAsync {
                    if (Updater.download(
                            Updater.updateUrl,
                            File(
                                "mods/${CrashPatch.NAME}-${
                                    Updater.latestTag.substringAfter(
                                        "v"
                                    )
                                }.jar"
                            )
                        ) && Updater.download(
                            "https://github.com/Wyvest/Deleter/releases/download/v1.2/Deleter-1.2.jar",
                            File(CrashPatch.modDir.parentFile, "Deleter-1.2.jar")
                        )
                    ) {
                        EssentialAPI.getNotifications()
                            .push(CrashPatch.NAME, "The ingame updater has successfully installed the newest version.")
                        Updater.addShutdownHook()
                        Updater.shouldUpdate = false
                    } else {
                        EssentialAPI.getNotifications().push(
                            CrashPatch.NAME,
                            "The ingame updater has NOT installed the newest version as something went wrong."
                        )
                    }
                }
            }
            this.onDeny = {
                restorePreviousScreen()
            }
        } childOf this.window
    }
}