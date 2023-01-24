package cc.woverflow.crashpatch.config

import cc.polyfrost.oneconfig.config.Config
import cc.polyfrost.oneconfig.config.annotations.Dropdown
import cc.polyfrost.oneconfig.config.annotations.Slider
import cc.polyfrost.oneconfig.config.annotations.Switch
import cc.polyfrost.oneconfig.config.data.Mod
import cc.polyfrost.oneconfig.config.data.ModType

object CrashPatchConfig : Config(Mod("CrashPatch", ModType.UTIL_QOL), "crashpatch.json") {
    @Switch(
        name = "Enable Initialization Crash Patch",
        description = "Catch crashes during initialization and display a message instead of crashing the game."
    )
    var initCrashPatch = true

    @Switch(
        name = "Enable In-Game Crash Patch",
        description = "Catch crashes during gameplay and display a message instead of crashing the game."
    )
    var inGameCrashPatch = true

    @Switch(
        name = "Enable Disconnect Crash Patch",
        description = "Display a message when a reason is found for a disconnect."
    )
    var disconnectCrashPatch = true

    @Slider(
        name = "Crash Limit",
        min = 1f,
        max = 100f
    )
    var crashLimit = 10

    @Switch(
        name = "Deobfuscate Crash Log",
        description = "Deobfuscate the crash log."
    )
    var deobfuscateCrashLog = true

    @Dropdown(
        name = "Crash Log Upload Method",
        description = "The method used to upload the crash log.",
        options = ["hst.sh", "mclo.gs (Aternos)"]
    )
    var crashLogUploadMethod = 0

    init {
        initialize()
    }
}