package org.polyfrost.crashpatch

import net.fabricmc.api.ClientModInitializer
import org.polyfrost.crashpatch.client.CrashPatchClient

class CrashPatchEntrypoint : ClientModInitializer {
    override fun onInitializeClient() {
        CrashPatchClient.initialize()
    }
}