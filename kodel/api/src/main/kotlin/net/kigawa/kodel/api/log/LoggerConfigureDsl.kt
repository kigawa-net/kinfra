package net.kigawa.kodel.api.log

class LoggerConfigureDsl {
    val loggerConfig = LoggerConfig()
    val children = mutableMapOf<String, LoggerConfigureDsl>()

    fun level(level: LogLevel?) {
        loggerConfig.level = level
    }

    fun handler(handler: LoggerHandlerDsl.() -> Unit) {
        LoggerHandlerDsl().apply(handler).handlerConfig.let {
            loggerConfig.addHandler(it)
        }
    }

    fun child(section: String, block: LoggerConfigureDsl.() -> Unit) {
        LoggerConfigureDsl().apply(block).let {
            children.put(section, it)
        }
    }
}
