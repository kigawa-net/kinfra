package net.kigawa.kodel.api.log

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.logging.LogRecord
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(markerClass = [ExperimentalTime::class])
actual data class LogRow actual constructor(
    actual val message: String,
    actual val level: LogLevel,
    actual val localDatetime: kotlinx.datetime.LocalDateTime,
    actual val sourceClassName: String,
    actual val sourceMethodName: String,
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun fromJvm(record: LogRecord): LogRow {
            return LogRow(
                message = record.message,
                level = record.level.let { LogLevel.fromJvm(it) },
                localDatetime = Instant.fromEpochMilliseconds(record.millis).toLocalDateTime(
                    TimeZone.currentSystemDefault()
                ),
                sourceClassName = record.sourceClassName,
                sourceMethodName = record.sourceMethodName,
            )
        }
    }

}