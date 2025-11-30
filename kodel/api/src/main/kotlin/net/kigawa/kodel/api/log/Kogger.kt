package net.kigawa.kodel.api.log

import java.util.logging.Logger

/**
 * ロガーインターフェース
 */
typealias Kogger = Logger

@Deprecated("use traceignore")
fun Logger.debug(msg: String) {
    this.fine(msg)
}

@Deprecated("use traceignore")
fun Logger.warn(msg: String) {
    this.warning(msg)
}

@Deprecated("use traceignore")
fun Logger.error(msg: String, e: Throwable? = null) {
    this.severe(msg)
    e?.printStackTrace()
}
