package org.polyfrost.crashpatch.client.crashes.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class CrashCause(
    val method: MatchMethod,
    val value: String,
) {
    companion object {
        @JvmField val CODEC: Codec<CrashCause> = RecordCodecBuilder.create { instance ->
            instance.group(
                MatchMethod.CODEC.fieldOf("method").forGetter(CrashCause::method),
                Codec.STRING.fieldOf("value").forGetter(CrashCause::value),
            ).apply(instance, ::CrashCause)
        }
    }

    fun matches(haystack: String): Boolean {
        return when (method) {
            MatchMethod.CONTAINS -> haystack.contains(value)
            MatchMethod.CONTAINS_NOT -> !haystack.contains(value)
            MatchMethod.REGEX -> Regex(value).containsMatchIn(haystack)
        }
    }
}
