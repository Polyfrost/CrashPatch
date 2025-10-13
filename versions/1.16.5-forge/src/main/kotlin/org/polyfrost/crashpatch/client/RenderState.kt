package org.polyfrost.crashpatch.client

import fudge.notenoughcrashes.utils.GlUtil

/**
 * Isn't used on 1.16.
 */
@Suppress("unused")
object RenderState {
    fun resetState() {
        GlUtil.resetState()
    }
}
