package net.kigawa.kodel.api.log

import java.util.logging.Level

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual enum class LogLevel constructor(
    val primary: Level, val secondary: List<Level> = listOf(),
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