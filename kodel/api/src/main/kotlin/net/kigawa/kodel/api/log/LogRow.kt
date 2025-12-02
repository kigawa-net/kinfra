package net.kigawa.kodel.api.log

import java.util.logging.LogRecord
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class LogRow @OptIn(ExperimentalTime::class) constructor(
    val message: String,
    val level: LogLevel,
    val instant: Instant,
    val sourceClassName: String,
    val sourceMethodName: String,
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun fromJvm(record: LogRecord): LogRow {
            return LogRow(
                message = record.message,
                level = record.level.let { LogLevel.fromJvm(it) },
                instant = Instant.fromEpochMilliseconds(record.millis),
                sourceClassName = record.sourceClassName,
                sourceMethodName = record.sourceMethodName,
            )
        }
    }
}