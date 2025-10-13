package org.polyfrost.crashpatch.client.crashes.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class FixType(
    val name: String,
    val noInGameDisplay: Boolean,
    val serverCrashes: Boolean
) {
    companion object {
        @JvmField val CODEC: Codec<FixType> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("name").forGetter(FixType::name),
                Codec.BOOL.optionalFieldOf("no_ingame_display", false).forGetter(FixType::noInGameDisplay),
                Codec.BOOL.optionalFieldOf("server_crashes", false).forGetter(FixType::serverCrashes),
            ).apply(instance, ::FixType)
        }
    }
}
