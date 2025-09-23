package org.polyfrost.crashpatch

import dev.deftu.omnicore.api.client.OmniDesktop
import org.polyfrost.oneconfig.api.config.v1.Config
import org.polyfrost.oneconfig.api.config.v1.annotations.*
import java.net.URI

object CrashPatchConfig : Config("crashpatch.json", "/assets/crashpatch/crashpatch_dark.svg", "CrashPatch", Category.QOL) {

    // Toggles
    @Switch(
        title = "Catch crashes during gameplay",
        description = "Catch crashes whilst in-game, and prevent the game from closing",
        subcategory = "Patches"
    )
    var inGameCrashPatch = true

    @Switch(
        title = "Patch crashes during launch",
        description = "Catch crashes during initialization, & display a message.",
        subcategory = "Patches"
    )
    var initCrashPatch = true

    @Switch(
        title = "Display disconnection causes",
        description = "Display a message when a reason is found for a disconnect.",
        subcategory = "Patches"
    )
    var disconnectCrashPatch = true

    @Info(
        title = "polyui.warning",
        description = "It's recommended to leave the world after a few crashes, and outright quit the game if there are more; this is to avoid severe instability",
        icon = "polyui/warning.svg",
        subcategory = "Limits"
    )
    var ignored: Boolean = false

    @Slider(
        title = "World Leave Limit",
        min = 1f,
        max = 20f,
        step = 1F,
        subcategory = "Limits"
    )
    var leaveLimit = 3

    @Slider(
        title = "Crash Limit",
        min = 1f,
        max = 20f,
        step = 1F,
        subcategory = "Limits"
    )
    var crashLimit = 5

    @Switch(
        title = "Deobfuscate Crash Log",
        description = "Makes certain class names more readable through deobfuscation",
        subcategory = "Logs"
    )
    var deobfuscateCrashLog = true

    @Dropdown(
        title = "Log uploader",
        description = "The method used to upload the crash log.",
        subcategory = "Logs"
    )
    var crashLogUploadMethod = UploadMethod.HASTEBIN

    @Button(
        title = "Polyfrost support",
        text = "Discord"
    )
    fun supportDiscord() {
        OmniDesktop.browse(URI.create("https://polyfrost.org/discord"))
    }

    enum class UploadMethod(val text: String) {
        HASTEBIN("hst.sh"),
        MCLOGS("mclo.gs (Aternos)")
    }

}