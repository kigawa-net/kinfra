package net.kigawa.kodel.api.log.config.formatter

import net.kigawa.kodel.api.log.LogRow

interface LoggerFormatter {
    fun format(row: LogRow): String
}