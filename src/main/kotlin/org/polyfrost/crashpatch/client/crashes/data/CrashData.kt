package org.polyfrost.crashpatch.client.crashes.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.Optional

data class CrashData(
    val fixes: List<CrashFix>,
    val fixTypes: List<FixType>,
    val defaultFixType: Int?,
) {
    companion object {
        @JvmField val CODEC: Codec<CrashData> = RecordCodecBuilder.create { instance ->
            instance.group(
                CrashFix.CODEC.listOf().fieldOf("fixes").forGetter(CrashData::fixes),
                FixType.CODEC.listOf().fieldOf("fixtypes").forGetter(CrashData::fixTypes),
                Codec.INT.optionalFieldOf("default_fix_type").forGetter { Optional.ofNullable(it.defaultFixType) },
            ).apply(instance) { fixes, fixTypes, defaultFixType ->
                CrashData(fixes, fixTypes, defaultFixType.orElse(null))
            }
        }
    }

    fun resolveId(fix: CrashFix): Int? {
        return fix.fixTypeId ?: defaultFixType
    }
}
