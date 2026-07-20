package net.kigawa.kodel.api.log.config.handler

import net.kigawa.kodel.api.log.LogRow
import net.kigawa.kodel.api.log.config.formatter.LoggerFormatter
import java.util.logging.Formatter
import java.util.logging.LogRecord

class JvmLoggerFormatter(
    val formatter: LoggerFormatter,
): Formatter() {


    override fun format(p0: LogRecord?): String? {
        return p0?.let { formatter.format(LogRow.fromJvm(it)) }
    }
}