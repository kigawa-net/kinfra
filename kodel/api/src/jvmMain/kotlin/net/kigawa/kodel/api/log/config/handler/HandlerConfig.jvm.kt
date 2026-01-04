package net.kigawa.kodel.api.log.config.handler

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.formatter.DefaultFormatter
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter
import net.kigawa.kodel.api.log.handler.LoggerHandler
import java.io.OutputStream
import java.io.PrintStream
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.StreamHandler

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING", "unused")
actual class HandlerConfig actual constructor(
    formatter: LoggerFormatter?, level: LogLevel?,
    val loggerHandler: (config: HandlerConfig) -> LoggerHandler,
): HandlerConfigCommon(formatter, level) {
    fun configureJvmStreamHandler(stream: PrintStream): StreamHandler {
        val formatter = JvmLoggerFormatter(formatter ?: DefaultFormatter)
        val handler = object: ConsoleHandler() {
            override fun setOutputStream(out: OutputStream?) {
                super.setOutputStream(stream)
            }
        }
        handler.formatter = formatter
        handler.level = level?.primary ?: Level.INFO
        return handler
    }

    actual override fun createLoggerHandler(): LoggerHandler {
        return loggerHandler(this)
    }

}