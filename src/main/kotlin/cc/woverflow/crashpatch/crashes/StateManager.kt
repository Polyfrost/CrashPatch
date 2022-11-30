package cc.woverflow.crashpatch.crashes

import java.lang.ref.WeakReference

/**
 * Allows registering objects to be reset after a crash. Objects registered
 * use WeakReferences, so they will be garbage-collected despite still being
 * registered here.
 */
object StateManager {
    // Use WeakReference to allow garbage collection, preventing memory leaks
    val resettableRefs: MutableSet<WeakReference<IResettable?>> = HashSet()
    fun resetStates() {
        val iterator = resettableRefs.iterator()
        while (iterator.hasNext()) {
            val ref = iterator.next()
            if (ref.get() != null) {
                ref.get()!!.resetState()
            } else {
                iterator.remove()
            }
        }
    }

    interface IResettable {
        fun resetState()
    }
}