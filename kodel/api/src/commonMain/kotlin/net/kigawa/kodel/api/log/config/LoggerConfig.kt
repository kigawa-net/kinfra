package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.*
import net.kigawa.kodel.api.log.config.handler.HandlerConfig


data class LoggerConfig(
    val level: LogLevel? = null,
    val handlerConfigs: List<HandlerConfig> = emptyList(),
) {

    fun configureLogger(kogger: Kogger) {
        configureLevel(kogger)
        configureHandlers(kogger)
    }

    private fun configureLevel(kogger: Kogger) {
        if (level == null) return
        kogger.also {
            println(it)
        }.logLevel = level
    }

    fun configureHandlers(kogger: Kogger) {
        if (handlerConfigs.isEmpty()) return
        kogger.apply {
            removeAllHandlers()
            handlerConfigs.forEach { config ->
                addHandler(config.createLoggerHandler())
            }
        }
    }
}
