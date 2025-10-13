package org.polyfrost.crashpatch.client.crashes.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional

data class CrashFix(
    val name: String?,
    val fixTypeId: Int?,
    val fixText: String,
    val causes: List<CrashCause>,
    val onlySkyClient: Boolean,
) {
    companion object {
        @JvmField val CODEC: Codec<CrashFix> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("name").forGetter { Optional.ofNullable(it.name) },
                Codec.INT.optionalFieldOf("fixtype").forGetter { Optional.ofNullable(it.fixTypeId) },
                Codec.STRING.fieldOf("fix").forGetter(CrashFix::fixText),
                CrashCause.CODEC.listOf().fieldOf("causes").forGetter(CrashFix::causes),
                Codec.BOOL.optionalFieldOf("onlySkyClient", false).forGetter(CrashFix::onlySkyClient),
            ).apply(instance) { name, fixTypeId, fixText, causes, onlySkyClient ->
                CrashFix(name.orElse(null), fixTypeId.orElse(null), fixText, causes, onlySkyClient)
            }
        }
    }

    fun triggersOn(haystack: String): Boolean {
        return causes.all { it.matches(haystack) }
    }
}
