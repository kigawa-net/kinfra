package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.handler.LoggerHandler

/**
 * ロガーインターフェース
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class Kogger {
    fun fine(msg: String)
    fun warning(msg: String)
    fun severe(msg: String)
}


expect var Kogger.logLevel: LogLevel
expect fun Kogger.removeAllHandlers()

expect fun Kogger.addHandler(handler: LoggerHandler)
