package org.polyfrost.crashpatch.client.crashes.data

import com.mojang.serialization.Codec

enum class MatchMethod {
    CONTAINS,
    CONTAINS_NOT,
    REGEX;

    companion object {
        @JvmField val CODEC: Codec<MatchMethod> = Codec.STRING.xmap(::from, MatchMethod::name)

        @Suppress("EnumValuesSoftDeprecate")
        fun from(name: String): MatchMethod {
            return values().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: CONTAINS
        }
    }
}
