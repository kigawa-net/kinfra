package net.kigawa.kodel.api.log.config.root

import net.kigawa.kodel.api.log.config.LoggerConfigureDsl
import net.kigawa.kodel.api.log.config.LoggerHandlerDsl

class RootLoggerConfigureDsl: LoggerConfigureDsl() {
    val rootLoggerConfig = RootLoggerConfig()
    fun rootConsoleHandler(block: LoggerHandlerDsl.() -> Unit){
        rootLoggerConfig.rootConsoleHandlerConfig = LoggerHandlerDsl().apply(block).handlerConfig
    }
}