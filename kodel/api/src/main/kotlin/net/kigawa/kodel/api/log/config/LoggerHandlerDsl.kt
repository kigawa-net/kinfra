package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.handler.LoggerHandler
import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter

class LoggerHandlerDsl {

    var formatter: LoggerFormatter? = null
    var level: LogLevel? = null
    fun asHandlerConfig(loggerHandler: (config: HandlerConfig) -> LoggerHandler) = HandlerConfig(
        formatter = formatter,
        level = level,
        loggerHandler = loggerHandler
    )
}