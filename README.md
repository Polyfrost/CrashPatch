# CrashPatch

[![Modrinth downloads](https://img.shields.io/modrinth/dt/nZ3E8WQz?style=flat&logo=modrinth&label=Modrinth&color=1bd96a)](https://modrinth.com/mod/crashpatch)
[![CurseForge downloads](https://img.shields.io/curseforge/dt/1147718?style=flat&logo=curseforge&label=CurseForge&color=f16436)](https://www.curseforge.com/minecraft/mc-mods/crashpatch)

CrashPatch keeps Minecraft open when the game crashes. It shows a screen that names the mod it suspects, lists known fixes and lets you return to the game.

## Features

### Crash screen

When the game crashes while you play, CrashPatch opens its own screen in place of closing the game. The screen has:

- The name of the mod CrashPatch suspects. It reads the name from the crash stack trace.
- Known fixes for the crash, sorted into tabs.
- A "Crash log" tab with the full crash report.
- A copy button and an upload button for the open tab.
- A "Return to game" button that closes the screen.
- A "Crash log" button that opens the saved report file.

### Startup crashes

The game cannot recover from a crash during launch. CrashPatch still opens the same screen with the suspected mod, the fixes and the log. The "Return to game" button becomes "Exit game".

### Disconnect reasons

When a server disconnects you and CrashPatch knows a fix for the reason, it replaces the disconnect screen with the reason and the fix. Any other disconnect shows the normal Minecraft screen. The "Display disconnection causes" option turns this off.

### Errors before the crash

CrashPatch watches the game log for mixins that failed to apply and for packets the game failed to handle. It keeps up to 5 of these errors. It adds them to the crash report under "Errors before the crash" and shows them in a tab on the crash screen. If the stack trace names no mod, CrashPatch takes the suspected mod from these errors.

### Commands

- `/crashpatch` opens the settings.
- `/crashpatch reload` downloads the list of known crashes again.
- `/crashpatch crash` crashes the game on purpose, so you can test the screen.

## Differences between 1.8.9 and Fabric

On 1.8.9, CrashPatch catches crashes itself. These options only work there:

- "Catch crashes during gameplay" and "Patch crashes during launch" turn the two crash screens on or off. Both are on by default.
- "World Leave Limit" is the number of crashes after which CrashPatch makes you leave the world. You set it between 1 and 20, and the default is 3.
- "Crash Limit" is the number of crashes after which CrashPatch lets the game close. You set it between 1 and 20, and the default is 5.

On Fabric, Not Enough Crashes catches the crash and CrashPatch replaces its screens. The four options above have no effect there. The first crash on a multiplayer server leaves you connected. A second crash within 10 seconds does not.

## Log uploads

CrashPatch uploads nothing until you click the upload button. The button sends the text of the open tab to hst.sh, or to mclo.gs if you pick it in the "Log uploader" option. CrashPatch removes your session ID and access token from the text first. It then copies the link to your clipboard and opens it in your browser.

## Supported versions

- Minecraft 1.8.9 on Ornithe.
- Minecraft 1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11, 26.1, 26.2 and 26.3 on Fabric.

CrashPatch needs [OneConfig](https://modrinth.com/mod/oneconfig) and Fabric Language Kotlin. On Fabric it also needs [Not Enough Crashes](https://modrinth.com/mod/notenoughcrashes).

## Support

- Report bugs and ask questions on the [Polyfrost Discord](https://polyfrost.org/discord).
- Check [Polyfrost Status](https://status.polyfrost.org/) if a Polyfrost service is down.

## Building

```sh
./gradlew ":26.3:build"
```

Replace `26.3` with any supported Minecraft version. The build needs JDK 21 and JDK 25 installed.
