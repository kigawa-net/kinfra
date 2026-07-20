package net.kigawa.kodel.api.log.config.handler

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter
import net.kigawa.kodel.api.log.handler.LoggerHandler

abstract class HandlerConfigCommon(
    val formatter: LoggerFormatter?,
    val level: LogLevel?,
) {
    abstract fun createLoggerHandler(): LoggerHandler
}
