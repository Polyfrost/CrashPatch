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
            if (ref.get() != null) {
                Objects.requireNonNull(ref.get())!!.resetState()
            } else {
                iterator.remove()
            }
        }
    }

    interface IResettable {
        fun register() {
            resettableRefs.add(WeakReference(this))
        }

        fun resetState()
    }
}