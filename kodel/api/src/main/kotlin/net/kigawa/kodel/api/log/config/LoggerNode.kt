package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LoggerFactory

class LoggerNode(
    val parent: LoggerNode?,
    val section: String,
) {
    val kogger by lazy { LoggerFactory.getInternal(name) }
    val children = mutableMapOf<String, LoggerNode>()
    val name: String
        get() {
            if (parent == null || parent.name == "") return section
            return "${parent.name}.$section"
        }

    fun configureDsl(configDsl: LoggerConfigureDsl) {
        configureConfig(configDsl.asLoggerConfig())
        configDsl.children.forEach { (key, value) ->
            children.getOrPut(key) {
                LoggerNode(this, key)
            }.configureDsl(value)
        }
    }

    fun configureConfig(config: LoggerConfig) = config.configureLogger(::kogger)
}