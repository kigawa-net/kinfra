package net.kigawa.kodel.api.log.config

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.LogRow
import net.kigawa.kodel.api.log.config.formatter.DefaultFormatter
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter

data class HandlerConfig(
    var formatter: LoggerFormatter = DefaultFormatter,
    var level: LogLevel? = null
) {
}
