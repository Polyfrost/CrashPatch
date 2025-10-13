package org.polyfrost.crashpatch.client.crashes

data class CrashScan(val solutions: List<Solution>) {
    data class Solution(
        val name: String,
        val solutions: List<String>,
        val isCrashReport: Boolean = false
    )
}
