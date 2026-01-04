package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.handler.LoggerHandler

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class Kogger {
    var logLevel: LogLevel = LogLevel.INFO
    val handlers = mutableListOf<LoggerHandler>()
    fun addHandler(handler: LoggerHandler) {
        handlers.add(handler)
    }

    fun removeAllHandlers() {
        handlers.clear()
    }

    actual fun fine(msg: String) {
        log(msg, LogLevel.DEBUG)
    }

    actual fun warning(msg: String) {
        log(msg, LogLevel.WARN)
    }

    actual fun severe(msg: String) {
        log(msg, LogLevel.ERROR)
    }

    fun log(msg: String, logLevel: LogLevel) {
        handlers.forEach {
            it.log(msg, logLevel)
        }
    }
}

actual var Kogger.logLevel: LogLevel
    get() = logLevel
    set(value) {
        logLevel = value
    }

actual fun Kogger.removeAllHandlers() {
    removeAllHandlers()
}

actual fun Kogger.addHandler(handler: LoggerHandler) {
    addHandler(handler)
}
