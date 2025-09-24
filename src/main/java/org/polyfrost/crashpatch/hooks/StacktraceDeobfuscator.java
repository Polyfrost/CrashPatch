package org.polyfrost.crashpatch.hooks;

//#if MC<1.13

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//TODO rewrite for fabric
public class StacktraceDeobfuscator {
    public static final StacktraceDeobfuscator INSTANCE = new StacktraceDeobfuscator();

    private static final boolean DEBUG_IN_DEV = false;

    private HashMap<String, String> srgMcpMethodMap = null;

    private StacktraceDeobfuscator() {
        File mappings = new File(new File(System.getProperty("user.dir")), "OneConfig/CrashPatch/mcp_stable_22.csv");
        mappings.mkdirs();
        if (!mappings.exists()) {
            HttpURLConnection connection = null;
            try {
                URL mappingsURL = new URL(
                        //#if MC==1.8.9
                        "https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable_nodoc/22-1.8.9/mcp_stable_nodoc-22-1.8.9.zip"
                        //#else
                        //$$ "http://export.mcpbot.bspk.rs/mcp_stable_nodoc/39-1.12/mcp_stable_nodoc-39-1.12.zip"
                        //#endif
                );
                connection = (HttpURLConnection) mappingsURL.openConnection();
                connection.setDoInput(true);
                connection.connect();
                try (ZipInputStream inputStream = new ZipInputStream(connection.getInputStream())) {
                    ZipEntry entry;
                    while ((entry = inputStream.getNextEntry()) != null) {
                        if (entry.getName().equals("methods.csv")) {
                            try (FileOutputStream out = new FileOutputStream(mappings)) {
                                byte[] buffer = new byte[2048];
                                int len;
                                while ((len = inputStream.read(buffer)) > 0) {
                                    out.write(buffer, 0, len);
                                }
                            }
                            break;
                        }
                    }
                    if (entry == null) {
                        throw new RuntimeException("Downloaded zip did not contain methods.csv");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        HashMap<String, String> srgMcpMethodMap = new HashMap<>();
        try (Scanner scanner = new Scanner(mappings)) {
            scanner.nextLine(); // Skip CSV header
            while (scanner.hasNext()) {
                String mappingLine = scanner.nextLine();
                int commaIndex = mappingLine.indexOf(',');
                String srgName = mappingLine.substring(0, commaIndex);
                String mcpName = mappingLine.substring(commaIndex + 1, commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(','));

                //System.out.println(srgName + " <=> " + mcpName);
                if (!DEBUG_IN_DEV) {
                    srgMcpMethodMap.put(srgName, mcpName);
                } else {
                    srgMcpMethodMap.put(mcpName, srgName);
                }
            }
        } catch (Exception e) {
            return;
        }
        this.srgMcpMethodMap = srgMcpMethodMap;
    }

    public void deobfuscateThrowable(Throwable throwable) {
        Deque<Throwable> queue = new ArrayDeque<>();
        queue.add(throwable);
        while (!queue.isEmpty()) {
            throwable = queue.remove();
            throwable.setStackTrace(deobfuscateStacktrace(throwable.getStackTrace()));
            if (throwable.getCause() != null) queue.add(throwable.getCause());
            Collections.addAll(queue, throwable.getSuppressed());
        }
    }

    public StackTraceElement[] deobfuscateStacktrace(StackTraceElement[] stackTrace) {
        int index = 0;
        for (StackTraceElement element : stackTrace) {
            stackTrace[index++] = new StackTraceElement(element.getClassName(), deobfuscateMethodName(element.getMethodName()), element.getFileName(), element.getLineNumber());
        }
        return stackTrace;
    }

    public String deobfuscateMethodName(String srgName) {
        if (srgMcpMethodMap == null) {
            return srgName; // Not initialized
        }
        String mcpName = srgMcpMethodMap.get(srgName);
        // log.debug(srgName + " <=> " + mcpName != null ? mcpName : "?"); // Can't do this, it would be a recursive call to log appender
        return mcpName != null ? mcpName : srgName;
    }
}
//#endif