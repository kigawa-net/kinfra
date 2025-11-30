package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.LogRow

class LoggerHandlerDsl {
    val handlerConfig = HandlerConfig()
    fun formater(function: (LogRow) -> String) {
        handlerConfig.formatter = function
    }
    fun level(level: LogLevel) {
        handlerConfig.level = level
    }
}