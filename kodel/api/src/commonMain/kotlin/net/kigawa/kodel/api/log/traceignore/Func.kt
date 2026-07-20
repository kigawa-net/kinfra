package net.kigawa.kodel.api.log.traceignore

import net.kigawa.kodel.api.log.Kogger


fun Kogger.debug(msg: String) {
    this.fine(msg)
}

fun Kogger.warn(msg: String) {
    this.warning(msg)
}

fun Kogger.error(msg: String, e: Throwable? = null) {
    this.severe(msg)
    e?.printStackTrace()
}
