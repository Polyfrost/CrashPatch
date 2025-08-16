package org.polyfrost.crashpatch.utils

import fudge.notenoughcrashes.utils.GlUtil as NECGlUtil

/**
 * Isn't used on 1.16.
 */
@Suppress("unused")
object GlUtil {
    fun resetState() {
        NECGlUtil.resetState()
    }
}