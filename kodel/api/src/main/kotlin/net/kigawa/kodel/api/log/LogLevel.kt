package net.kigawa.kodel.api.log

import java.util.logging.Level

/**
 * ログレベル
 */
enum class LogLevel(
    val jvm: Level,
) {
    DEBUG(Level.FINE),
    INFO(Level.INFO),
    WARN(Level.WARNING),
    ERROR(Level.SEVERE),
    ;

    companion object {
        fun fromJvm(level: Level): LogLevel? {
            return entries.find { it.jvm == level }
        }
    }
}
