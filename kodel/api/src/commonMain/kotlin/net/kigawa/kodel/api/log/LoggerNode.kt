package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.config.LoggerConfig
import net.kigawa.kodel.api.log.config.LoggerConfigureDsl

class LoggerNode(
    val parent: LoggerNode?,
    val section: String,
    config: LoggerConfig?,
) {
    private var koggerInternal: Kogger? = null
    val kogger: Kogger
        get() {
            koggerInternal?.let { return it }
            return NativeLoggerAdaptor.getKogger(name).also {
                koggerInternal = it
                configureConfig()
            }
        }
    private var config: LoggerConfig? = config
        set(value) {
            field = value
            children.values.forEach { it.config = value }
        }
    private val children = mutableMapOf<String, LoggerNode>()
    val name: String
        get() {
            if (parent == null || parent.name == "") return section
            return "${parent.name}.$section"
        }

    fun getChild(section: String) = children.getOrPut(section) { LoggerNode(this, section, config) }
    fun configureDsl(configDsl: LoggerConfigureDsl) {
        config = configDsl.asLoggerConfig()
        configureConfig()
        configDsl.children.forEach { (key, value) ->
            getChild(key).configureDsl(value)
        }
    }

    fun configureConfig() {
        val k = koggerInternal ?: return
        val c = config ?: return
        c.configureLogger(k)
    }
}
