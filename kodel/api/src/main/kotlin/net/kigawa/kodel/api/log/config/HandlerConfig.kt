package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.formatter.DefaultFormatter
import net.kigawa.kodel.api.log.config.formatter.JvmLoggerFormatter
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter
import net.kigawa.kodel.api.log.handler.LoggerHandler
import java.io.OutputStream
import java.io.PrintStream
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.StreamHandler

data class HandlerConfig(
    val formatter: LoggerFormatter?,
    val level: LogLevel?,
    val loggerHandler: (config: HandlerConfig) -> LoggerHandler,
) {
    fun configureHandler(logger: Kogger) = loggerHandler(this).configure(logger)
    fun configureJvmStreamHandler(stream: PrintStream): StreamHandler {
        val formatter = JvmLoggerFormatter(formatter ?: DefaultFormatter)
        val handler = object : ConsoleHandler(){
            override fun setOutputStream(out: OutputStream?) {
                super.setOutputStream(stream)
            }
        }
        handler.formatter = formatter
        handler.level = level?.primary ?: Level.INFO
        return handler
    }
}
