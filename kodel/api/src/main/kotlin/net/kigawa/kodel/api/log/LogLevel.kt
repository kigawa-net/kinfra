package net.kigawa.kodel.api.log

import java.util.logging.Level

/**
 * ログレベル
 */
enum class LogLevel(
    val primary: Level,
    val secondary: List<Level> = emptyList(),
) {
    DEBUG(Level.FINE, listOf(Level.FINER, Level.FINEST)),
    INFO(Level.INFO),
    WARN(Level.WARNING),
    ERROR(Level.SEVERE),
    ;

    companion object {
        fun fromJvm(level: Level): LogLevel {
            return entries.find { it.primary == level || it.secondary.contains(level) }
                ?: throw IllegalArgumentException("unknown level $level")
        }
    }
}
