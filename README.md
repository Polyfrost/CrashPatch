# CrashPatch

<a href="https://github.com/W-OVERFLOW/CrashPatch/releases" target="_blank"></a>
<img alt="downloads" src="https://img.shields.io/github/downloads/W-OVERFLOW/CrashPatch/total?color=F5C400&style=for-the-badge" /> <img alt="downloads latest" src="https://img.shields.io/github/downloads-pre/W-OVERFLOW/CrashPatch/latest/total?color=F5C400&style=for-the-badge"/> ![OneConfig Supported](https://i.imgur.com/pFqMYWp.png)
![Dev Workflow Status](https://img.shields.io/github/v/release/Polyfrost/CrashPatch.svg?style=for-the-badge&color=1452cc&label=release)

### Stop crashes from closing your game!

CrashPatch is a Minecraft (1.8.9) modification port of the BetterCrashes Minecraft modification (1.7.10) which is a port of the VanillaFix Minecraft modification (1.12.2).

CrashPatch essentially wraps the game in a try catch which stops the game from closing when something goes wrong (aka a crash).

When CrashPatch detects a crash, it will display a screen where you can either return playing the game, find solutions to the crash, or open / upload the crash log.

If Minecraft crashes while loading / initializing the game, it will not be able to recover but the ability to see possible solutions and / or open and upload crash logs will be available.

Modifiable options include:
- Whether to catch crashes during launch or gameplay
- Display disconnect causes
- Crash limit before force closing
- Whether to deobfuscate crash log
- Where to upload logs
