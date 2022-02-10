package gg.essential.util.crash;

public abstract class StacktraceDeobfuscator {
    public static StacktraceDeobfuscator get() {
        throw new AssertionError();
    }

    public abstract void deobfuscateThrowable(Throwable t);
}
