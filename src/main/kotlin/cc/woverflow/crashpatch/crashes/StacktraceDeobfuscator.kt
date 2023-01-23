package cc.woverflow.crashpatch.crashes

import cc.woverflow.crashpatch.hooks.McDirUtil
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.collections.set

object StacktraceDeobfuscator {
    private const val DEBUG_IN_DEV =
        true // Makes this MCP -> SRG for testing in dev. Don't forget to set to false when done!

    private var srgMcpMethodMap: HashMap<String, String>? = null

    init {
        if (srgMcpMethodMap == null) {
            val mappings = File(McDirUtil.getMcDir(), "OneConfig/CrashPatch/mcp_stable_22.csv").also { it.parentFile.mkdirs() }
            // Download the file if necessary
            if (!mappings.exists()) {
                var connection: HttpURLConnection? = null
                try {
                    val mappingsURL =
                        URL("https://maven.minecraftforge.net/de/oceanlabs/mcp/mcp_stable_nodoc/22-1.8.9/mcp_stable_nodoc-22-1.8.9.zip")
                    connection = mappingsURL.openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()
                    connection.inputStream.use { inputStream ->
                        val zipInputStream = ZipInputStream(inputStream)
                        var entry: ZipEntry?
                        while (zipInputStream.nextEntry.also { entry = it } != null) {
                            if (entry!!.name == "methods.csv") {
                                FileOutputStream(mappings).use { out ->
                                    val buffer = ByteArray(2048)
                                    var len: Int
                                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                                        out.write(buffer, 0, len)
                                    }
                                }
                                break
                            }
                        }
                        if (entry == null) {
                            throw RuntimeException("Downloaded zip did not contain methods.csv")
                        }
                    }
                } catch (e: IOException) {
                    throw RuntimeException(e)
                } finally {
                    connection?.disconnect()
                }
            }

            // Read the mapping
            val srgMcpMethodMap: HashMap<String, String> = HashMap()
            try {
                Scanner(mappings).use { scanner ->
                    scanner.nextLine() // Skip CSV header
                    while (scanner.hasNext()) {
                        val mappingLine = scanner.nextLine()
                        val commaIndex = mappingLine.indexOf(',')
                        val srgName = mappingLine.substring(0, commaIndex)
                        val mcpName = mappingLine.substring(
                            commaIndex + 1,
                            commaIndex + 1 + mappingLine.substring(commaIndex + 1).indexOf(',')
                        )

                        //System.out.println(srgName + " <=> " + mcpName);
                        if (!DEBUG_IN_DEV) {
                            srgMcpMethodMap[srgName] = mcpName
                        } else {
                            srgMcpMethodMap[mcpName] = srgName
                        }
                    }
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            // Set the map only if it's successful, to make sure that it's complete
            StacktraceDeobfuscator.srgMcpMethodMap = srgMcpMethodMap
        }
    }

    fun deobfuscateThrowable(throwable: Throwable) {
        var t = throwable
        val queue: Deque<Throwable> = ArrayDeque()
        queue.add(t)
        while (!queue.isEmpty()) {
            t = queue.remove()
            t.stackTrace = deobfuscateStacktrace(t.stackTrace)
            if (t.cause != null) queue.add(t.cause)
            Collections.addAll(queue, *t.suppressed)
        }
    }

    fun deobfuscateStacktrace(stackTrace: Array<StackTraceElement>): Array<StackTraceElement> {
        var index = 0
        for (el in stackTrace) {
            stackTrace[index++] =
                StackTraceElement(el.className, deobfuscateMethodName(el.methodName), el.fileName, el.lineNumber)
        }
        return stackTrace
    }

    fun deobfuscateMethodName(srgName: String): String {
        if (srgMcpMethodMap == null) {
            return srgName // Not initialized
        }
        val mcpName = srgMcpMethodMap!![srgName]
        // log.debug(srgName + " <=> " + mcpName != null ? mcpName : "?"); // Can't do this, it would be a recursive call to log appender
        return mcpName ?: srgName
    }
}