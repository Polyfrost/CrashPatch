plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2" /* [SC] DO NOT EDIT */

stonecutter {
    tasks {
        order("publishModrinth")
    }

    parameters {
        replacements {
            string(eval(current.version, "= 1.8.9")) {
                replace(
                    "net.minecraft.CrashReport",
                    "net.minecraft.util.crash.CrashReport"
                )
                replace(
                    "net.minecraft.server.Bootstrap",
                    "net.minecraft.Bootstrap"
                )
                replace(
                    "net.minecraft.client.gui.screens.Screen",
                    "net.minecraft.client.gui.screen.Screen"
                )
                replace(
                    "net.minecraft.client.gui.screens.DisconnectedScreen",
                    "net.minecraft.client.gui.screen.DisconnectedScreen"
                )
                replace(
                    "net.minecraft.client.resources.language.I18n",
                    "net.minecraft.client.resource.language.I18n"
                )
                replace(
                    "net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback",
                    "org.polyfrost.oneconfig.internal.legacy.command.ClientCommandRegistrationCallback"
                )
                replace(
                    "net.minecraft.network.chat.Component",
                    "org.polyfrost.oneconfig.internal.legacy.chat.Component"
                )
                replace(
                    "net.minecraft.ChatFormatting",
                    "org.polyfrost.oneconfig.internal.legacy.chat.ChatFormatting"
                )
            }
        }
    }
}
