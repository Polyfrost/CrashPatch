/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/client/MixinMinecraft.java
 *The source file uses the MIT License.
 */

package cc.woverflow.crashpatch.crashes

import java.lang.ref.WeakReference

/**
 * Allows registering objects to be reset after a crash. Objects registered
 * use WeakReferences, so they will be garbage-collected despite still being
 * registered here.
 */
object StateManager {
    // Use WeakReference to allow garbage collection, preventing memory leaks
    private val resettableRefs: MutableSet<WeakReference<IResettable?>> = HashSet()
    fun resetStates() {
        val iterator = resettableRefs.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            ref.get()?.resetState() ?: iterator.remove()
        }
    }

    interface IResettable {

        fun register() {
            resettableRefs.add(WeakReference(this))
        }

        fun resetState()
    }
}