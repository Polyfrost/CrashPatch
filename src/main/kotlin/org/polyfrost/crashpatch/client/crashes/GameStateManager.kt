package org.polyfrost.crashpatch.client.crashes

//#if MC < 1.13
import java.lang.ref.WeakReference

/**
 * Allows registering objects to be reset after a crash. Objects registered
 * use WeakReferences, so they will be garbage-collected despite still being
 * registered here.
 *
 * Uses WeakReference to allow garbage collection, preventing memory leaks
 */
object GameStateManager {
    interface ResettableObject {
        fun resetGameState()
    }

    /**
     * For objects that are part of the game. Exists so that we can add a prefix to the function name, as per
     * the standard for Mixin hooks
     */
    interface ResettableGameObject : ResettableObject {
        @Suppress("FunctionName")
        fun `crashpatch$resetGameState`()

        override fun resetGameState() {
            `crashpatch$resetGameState`()
        }
    }

    private val resettableRefs: MutableSet<WeakReference<ResettableObject?>> = HashSet()

    @JvmStatic
    fun resetStates() {
        val iterator = resettableRefs.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            if (ref.get() != null) {
                ref.get()!!.resetGameState()
            } else {
                iterator.remove()
            }
        }
    }

    @JvmStatic
    fun register(obj: ResettableObject) {
        resettableRefs.add(WeakReference(obj))
    }
}
//#endif
