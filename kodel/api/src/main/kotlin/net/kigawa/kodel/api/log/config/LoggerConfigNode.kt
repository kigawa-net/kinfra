package net.kigawa.kodel.api.log.config

class LoggerConfigNode(
    val parent: LoggerConfigNode?,
    val section: String,
    var config: LoggerConfig,
) {
    val children = mutableMapOf<String, LoggerConfigNode>()
    val name: String
        get() {
            if (parent == null || parent.name == "") return section
            return "${parent.name}.$section"
        }

    fun configure(config: LoggerConfigureDsl) {
        this.config = config.loggerConfig
        config.children.forEach { (key, value) ->
            children.getOrPut(key) {
                LoggerConfigNode(this, key, LoggerConfig())
            }.configure(value)
        }
    }
}