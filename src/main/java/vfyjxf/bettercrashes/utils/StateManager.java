package vfyjxf.bettercrashes.utils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class StateManager {
    public interface IResettable {
        default void register() {
            resettableRefs.add(new WeakReference<>(this));
        }

        void resetState();
    }

    // Use WeakReference to allow garbage collection, preventing memory leaks
    private static Set<WeakReference<IResettable>> resettableRefs = new HashSet<>();

    public static void resetStates() {
        Iterator<WeakReference<IResettable>> iterator = resettableRefs.iterator();
        while (iterator.hasNext()) {
            WeakReference<IResettable> ref = iterator.next();
            if (ref.get() != null) {
                ref.get().resetState();
            } else {
                iterator.remove();
            }
        }
    }
}
