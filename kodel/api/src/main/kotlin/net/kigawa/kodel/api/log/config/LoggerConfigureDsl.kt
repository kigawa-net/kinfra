package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LogLevel

open class LoggerConfigureDsl {
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
        val sections = section.split(".").toMutableList()
        val first = sections.removeFirst()

        children.getOrPut(first, ::LoggerConfigureDsl)
            .apply {
                if (sections.isEmpty()) block()
                else child(sections.joinToString("."), block)
            }
    }
}
