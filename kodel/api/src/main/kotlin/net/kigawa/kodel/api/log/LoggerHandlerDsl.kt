package net.kigawa.kodel.api.log

class LoggerHandlerDsl {
    val handlerConfig = HandlerConfig()
    fun formater(function: (LogRow) -> String) {
        handlerConfig.formatter = function
    }
    fun level(level: LogLevel) {
        handlerConfig.level = level
    }
}