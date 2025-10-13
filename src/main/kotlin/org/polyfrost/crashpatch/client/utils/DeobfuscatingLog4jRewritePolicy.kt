package org.polyfrost.crashpatch.client.utils

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.appender.rewrite.RewriteAppender
import org.apache.logging.log4j.core.appender.rewrite.RewritePolicy
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.LoggerConfig
import org.polyfrost.crashpatch.client.CrashPatchConfig
import org.polyfrost.crashpatch.hooks.StacktraceDeobfuscator

class DeobfuscatingLog4jRewritePolicy : RewritePolicy {
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
                DeobfuscatingLog4jRewritePolicy(),
                null
            )

            rewriteAppender.start()

            // Add the new appender
            loggerConfig.addAppender(rewriteAppender, null, null)
        }
    }

    override fun rewrite(source: LogEvent): LogEvent {
        if (CrashPatchConfig.deobfuscateCrashLog) {
            source.thrown?.let { StacktraceDeobfuscator.INSTANCE.deobfuscateThrowable(it) }
        }

        return source
    }
}
