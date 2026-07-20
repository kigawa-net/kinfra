package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.handler.LoggerHandler

open class LoggerConfigureDsl {
    val children = mutableMapOf<String, LoggerConfigureDsl>()

    var level : LogLevel? = null
    val handlers = mutableListOf<HandlerConfig>()

    fun handler(handler: (config: HandlerConfig) -> LoggerHandler, function: LoggerHandlerDsl.() -> Unit) {
        LoggerHandlerDsl().apply(function).asHandlerConfig(handler).let {
            handlers.add(it)
        }
    }
    fun asLoggerConfig() = LoggerConfig(level, handlers)

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
