package org.polyfrost.crashpatch.gui

import cc.polyfrost.oneconfig.renderer.font.Font
import cc.polyfrost.oneconfig.utils.color.ColorUtils

internal val GRAY_800 = ColorUtils.getColor(21, 22, 23, 255) // general background
internal val GRAY_700 = ColorUtils.getColor(34, 35, 38, 255) // log background
internal val GRAY_600 = ColorUtils.getColor(42, 44, 48, 255) // log header

internal val WHITE_90 = ColorUtils.getColor(255, 255, 255, 229) // text
internal val WHITE_80 = ColorUtils.getColor(255, 255, 255, 204) // subtext
internal val WHITE_60 = ColorUtils.getColor(255, 255, 255, 153) // logs

internal val BLUE_400 = ColorUtils.getColor(77, 135, 229) // yeah
internal val BLUE_600 = ColorUtils.getColor(20, 82, 204, 255) // brand.hover

internal val HYPERLINK_BLUE = ColorUtils.getColor(48, 129, 242)

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

internal val JETBRAINS_MONO = Font("jetbrains-mono-regular", "/assets/crashpatch/fonts/JetBrainsMono-Regular.ttf")