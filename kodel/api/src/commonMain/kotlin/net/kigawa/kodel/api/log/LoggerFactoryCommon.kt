package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.config.root.RootLoggerConfigureDsl

abstract class LoggerFactoryCommon {
    val loggerNode = LoggerNode(
        null, "", null
    )

    fun get(name: String): Kogger = name
        .split(".")
        .filter { it.isNotBlank() }
        .fold(loggerNode) { node, section ->
            node.getChild(section)
        }.kogger


    protected abstract fun configureRoot()

    @Suppress("unused")
    fun configure(block: RootLoggerConfigureDsl.() -> Unit) {
        RootLoggerConfigureDsl().apply(block).let {
            configureRoot()
            loggerNode.configureDsl(it)
        }
    }
}
