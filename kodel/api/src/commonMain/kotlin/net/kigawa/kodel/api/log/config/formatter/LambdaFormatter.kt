package net.kigawa.kodel.api.log.config.formatter

import net.kigawa.kodel.api.log.LogRow

@Suppress("unused")
class LambdaFormatter(
    val function: (LogRow) -> String
): LoggerFormatter {
    override fun format(row: LogRow): String {
        return function(row)
    }
}