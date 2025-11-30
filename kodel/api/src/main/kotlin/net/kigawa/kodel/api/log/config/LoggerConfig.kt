package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LogLevel

data class LoggerConfig(
    var level: LogLevel? = null,
    val handlers: MutableList<HandlerConfig> = mutableListOf(),
) {
    fun addHandler(handlerConfig: HandlerConfig) {
        handlers.add(handlerConfig)
    }
}
