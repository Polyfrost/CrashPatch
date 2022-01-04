package net.wyvest.crashpatch.crashes

import java.lang.ref.WeakReference
import java.util.*

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