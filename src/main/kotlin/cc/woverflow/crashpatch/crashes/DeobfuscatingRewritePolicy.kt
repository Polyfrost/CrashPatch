/*
 *This file is modified based on
 *https://github.com/DimensionalDevelopment/VanillaFix/blob/master/src/main/java/org/dimdev/vanillafix/crashes/mixins/client/MixinMinecraft.java
 *The source file uses the MIT License.
 */

package cc.woverflow.crashpatch.crashes

import gg.essential.util.crash.StacktraceDeobfuscator
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig

class DeobfuscatingRewritePolicy : RewritePolicy {
    override fun rewrite(source: LogEvent): LogEvent {
        source.thrown?.let { StacktraceDeobfuscator.get()?.deobfuscateThrowable(it) }
        return source
    }

    companion object {
        fun install() {
            val rootLogger = LogManager.getRootLogger() as Logger
            val loggerConfig: LoggerConfig = rootLogger.context.configuration.getLoggerConfig(rootLogger.name)

            // Remove appender refs from config
            val appenderRefs: List<AppenderRef> = ArrayList(loggerConfig.appenderRefs)
            for (appenderRef in appenderRefs) {
                loggerConfig.removeAppender(appenderRef.ref)
            }

            // Create the RewriteAppender, which wraps the appenders
            val rewriteAppender = RewriteAppender.createAppender(
                "CrashPatchDeobfuscatingAppender",
                "true",
                appenderRefs.toTypedArray(),
                rootLogger.context.configuration,
                DeobfuscatingRewritePolicy(),
                null
            )
            rewriteAppender.start()

            // Add the new appender
            loggerConfig.addAppender(rewriteAppender, null, null)
        }
    }
}
