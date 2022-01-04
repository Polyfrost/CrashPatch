package net.wyvest.crashpatch.utils

import com.google.gson.*

private val parser = JsonParser()

fun String.asJsonObject(): JsonObject = parser.parse(this).asJsonObject

val JsonObject.keys: List<String>
    get() {
        val keys: MutableList<String> = ArrayList()
        val entrySet: Set<Map.Entry<String, JsonElement?>> = this.entrySet()
        for ((key) in entrySet) {
            keys.add(key)
        }
        return keys
    }

operator fun JsonObject.get(key: String, value: String? = ""): String? {
    if (key.isEmpty() || !has(key)) return value
    val prim = asPrimitive(get(key))
    return if (prim != null && prim.isString) prim.asString else value
}

operator fun JsonObject.get(key: String, value: JsonArray? = JsonArray()): JsonArray? {
    if (key.isEmpty() || !has(key)) return value
    return getAsJsonArray(key)
}

operator fun JsonObject.get(key: String, value: Boolean = false): Boolean {
    if (key.isEmpty() || !has(key)) return value

    val prim: JsonPrimitive? = asPrimitive(get(key))
    return if (prim != null && prim.isBoolean) {
        prim.asBoolean
    } else value
}

fun asPrimitive(element: JsonElement): JsonPrimitive? {
    return if (element is JsonPrimitive) element.getAsJsonPrimitive() else null
}

fun String.containsAny(vararg sequences: CharSequence?) = sequences.any { it != null && this.contains(it, true) }