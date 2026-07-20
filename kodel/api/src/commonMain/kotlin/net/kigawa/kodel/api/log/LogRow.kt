package net.kigawa.kodel.api.log

import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@OptIn(ExperimentalTime::class)
expect  class LogRow(
     message: String,
    level: LogLevel,
    localDatetime: LocalDateTime,
    sourceClassName: String,
    sourceMethodName: String,
) {
    val sourceMethodName: String
    val sourceClassName: String
    val localDatetime: LocalDateTime
    val level: LogLevel
    val message: String
}