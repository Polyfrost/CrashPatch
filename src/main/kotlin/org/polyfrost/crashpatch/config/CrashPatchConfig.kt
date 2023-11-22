package org.polyfrost.crashpatch.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.*
import cc.polyfrost.oneconfig.config.data.InfoType
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType
import cc.polyfrost.oneconfig.libs.universal.UDesktop.browse
import java.net.URI


object CrashPatchConfig : Config(Mod("CrashPatch", ModType.UTIL_QOL, "/assets/crashpatch/crashpatch_dark.svg"), "crashpatch.json") {

    // Toggles
    @Switch(
        name = "Catch crashes during gameplay",
        description = "Catch crashes whilst in-game, and prevent the game from closing",
        subcategory = "Patches"
    )
    var inGameCrashPatch = true

    @Switch(
        name = "Patch crashes during launch",
        description = "Catch crashes during initialization, & display a message.",
        subcategory = "Patches"
    )
    var initCrashPatch = true

    @Switch(
        name = "Display disconnection causes",
        description = "Display a message when a reason is found for a disconnect.",
        subcategory = "Patches"
    )
    var disconnectCrashPatch = true

    // Limits
    @Info(
        text = "It's recommended to restart your game after every few crashes, to avoid severe instability",
        type = InfoType.WARNING,
        size = 2,
        subcategory = "Limits"
    )
    var ignored: Boolean = false


    @Slider(
        name = "Crash Limit",
        min = 1f,
        max = 20f,
        step = 1,
        subcategory = "Limits"
    )
    var crashLimit = 5

    @Switch(
        name = "Deobfuscate Crash Log",
        description = "Makes certain class names more readable through deobfuscation",
        size = 2,
        subcategory = "Logs"
    )
    var deobfuscateCrashLog = true

    @Dropdown(
        name = "Log uploader",
        description = "The method used to upload the crash log.",
        options = ["hst.sh", "mclo.gs (Aternos)"],
        subcategory = "Logs"
    )
    var crashLogUploadMethod = 0

    @Button(
        name = "Polyfrost support",
        text = "Discord"
    )
    var supportDiscord = Runnable { browse(URI.create("https://polyfrost.cc/discord/")) }

    init {
        initialize()
    }
}