/*
 *This file is from
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/StateManager.java
 *The source file uses the MIT License.
 */

package vfyjxf.bettercrashes.utils;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Runemoro
 */
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
