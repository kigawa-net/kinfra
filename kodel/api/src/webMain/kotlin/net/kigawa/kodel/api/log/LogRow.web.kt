package net.kigawa.kodel.api.log

import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual data class LogRow @OptIn(markerClass = [ExperimentalTime::class]) actual constructor(
    actual val message: String, actual val level: LogLevel,
    actual val localDatetime: LocalDateTime, actual val sourceClassName: String,
    actual val sourceMethodName: String,
)