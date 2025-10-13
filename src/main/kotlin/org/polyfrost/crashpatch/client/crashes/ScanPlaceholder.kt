package org.polyfrost.crashpatch.client.crashes

data class ScanPlaceholder<T>(
    val name: String,
    val value: () -> T
) {
    fun replace(text: String): String {
        return text.replace("%${name}%", value.invoke().toString())
    }
}
