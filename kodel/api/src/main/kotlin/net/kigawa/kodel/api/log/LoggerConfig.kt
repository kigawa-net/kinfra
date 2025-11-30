package net.kigawa.kodel.api.log

data class LoggerConfig(
    var level: LogLevel? = null,
    val handlers: MutableList<HandlerConfig> = mutableListOf(),
) {
    fun addHandler(handlerConfig: HandlerConfig) {
        handlers.add(handlerConfig)
    }
}
