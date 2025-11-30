package net.kigawa.kodel.api.log.traceignore

import java.util.logging.Logger


fun Logger.debug(msg: String) {
    this.fine(msg)
}

fun Logger.warn(msg: String) {
    this.warning(msg)
}

fun Logger.error(msg: String, e: Throwable? = null) {
    this.severe(msg)
    e?.printStackTrace()
}
