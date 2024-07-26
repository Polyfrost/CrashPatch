package org.polyfrost.crashpatch.gui

import org.polyfrost.polyui.utils.rgba

internal val GRAY_800 = rgba(21, 22, 23, 1f) // general background
internal val GRAY_700 = rgba(34, 35, 38, 1f) // log background
internal val GRAY_600 = rgba(42, 44, 48, 1f) // log header

internal val WHITE_90 = rgba(255, 255, 255, 229 / 255f) // text
internal val WHITE_80 = rgba(255, 255, 255, 204 / 255f) // subtext
internal val WHITE_60 = rgba(255, 255, 255, 153 / 255f) // logs

internal val BLUE_400 = rgba(77, 135, 229) // yeah
internal val BLUE_600 = rgba(20, 82, 204, 1F) // brand.hover

internal val HYPERLINK_BLUE = rgba(48, 129, 242)

internal val TITLE = "Uh-oh. Your game crashed!"
internal val DISCONNECTED_TITLE = "Uh-oh. You were disconnected from the server!"
internal val SUBTITLE_1 = "But, CrashPatch just saved the day! Feel free to ignore this, and"
internal val SUBTITLE_2 = "continue playing your game despite the crash."
internal val SUBTITLE_DISCONNECTED = "The full reason is below, but"
internal val SUBTITLE_DISCONNECTED_2 = "you can ignore this and continue playing."
internal val SUBTITLE_INIT_1 = "To fix this, "
internal val SUBTITLE_INIT_2 = "follow the tips listed below and / or "
internal val SUBTITLE_INIT_3 = "join the Discord server and make a support ticket."

internal val CAUSE_TEXT = "This could have been caused by:"
internal val CAUSE_TEXT_DISCONNECTED = "Reason for disconnect:"

internal val RETURN_TO_GAME = "Return to game"
internal val OPEN_CRASH_LOG = "Crash log"

internal val SKYCLIENT_DISCORD = "https://inv.wtf/skyclient"
internal val POLYFROST_DISCORD = "https://polyfrost.cc/discord"