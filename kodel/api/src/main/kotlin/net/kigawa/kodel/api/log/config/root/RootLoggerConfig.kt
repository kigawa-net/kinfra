package net.kigawa.kodel.api.log.config.root

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.HandlerConfig

data class RootLoggerConfig(
    var rootConsoleHandlerConfig: HandlerConfig = HandlerConfig(
        level = LogLevel.INFO,
    ),
) {
}