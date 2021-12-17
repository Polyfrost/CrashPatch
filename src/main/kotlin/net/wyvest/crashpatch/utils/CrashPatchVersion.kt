package net.wyvest.crashpatch.utils

import net.wyvest.crashpatch.CrashPatch
import java.text.ParseException

/**
 * A class which represents a version of CrashPatch.
 *
 * <p>
 *
 * Adapted from Kotlin under the Apache License 2.0
 * https://github.com/JetBrains/kotlin/blob/master/license/LICENSE.txt
 */
data class CrashPatchVersion(val major: Int, val minor: Int, val patch: Int, val beta: Int) : Comparable<CrashPatchVersion> {

    private val version = versionOf(major, minor, patch, beta)

    private fun versionOf(major: Int, minor: Int, patch: Int, beta: Int): Int {
        require(major in 0..MAX_COMPONENT_VALUE && minor in 0..MAX_COMPONENT_VALUE && patch in 0..MAX_COMPONENT_VALUE && beta in 0..MAX_COMPONENT_VALUE) {
            "Version components are out of range: $major.$minor.$patch-beta$beta"
        }
        return major.shl(16) + minor.shl(8) + patch + beta
    }

    override fun toString(): String = "$major.$minor.$patch-beta$beta"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherVersion = (other as? CrashPatchVersion) ?: return false
        return this.version == otherVersion.version
    }

    override fun hashCode(): Int = version

    override fun compareTo(other: CrashPatchVersion): Int = version - other.version

    companion object {
        const val MAX_COMPONENT_VALUE = 255
        val regex =
            Regex("^(?<major>[0|1-9\\d*])\\.(?<minor>[0|1-9\\d*])\\.(?<patch>[0|1-9\\d*])(?:-beta)?(?<beta>.*)?\$")

        val CURRENT: CrashPatchVersion = fromString(CrashPatch.VERSION)

        fun fromString(version: String): CrashPatchVersion {
            val match = regex.matchEntire(version)
            if (match != null) {
                return CrashPatchVersion(
                    match.groups["major"]!!.value.toInt(),
                    match.groups["minor"]!!.value.toInt(),
                    match.groups["patch"]!!.value.toInt(),
                    if (match.groups["beta"]?.value.isNullOrBlank()) 0 else match.groups["beta"]!!.value.toInt()
                )
            } else {
                throw ParseException("The string ($version) provided did not match the ${CrashPatch.NAME} Version regex!", -1)
            }
        }
    }
}