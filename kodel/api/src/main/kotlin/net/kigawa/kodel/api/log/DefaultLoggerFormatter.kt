package net.kigawa.kodel.api.log

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.Formatter
import java.util.logging.LogRecord

class DefaultLoggerFormatter: Formatter() {
    companion object {
        const val MAX_PACKAGE_SECTION_LENGTH = 40
    }

    override fun format(p0: LogRecord?): String? {
        return p0?.run {
            val lvStr = LogLevel.fromJvm(level)
                .let { it?.name ?: level.name }
                .padEnd(8)
            val className = formatClassName(sourceClassName)
            val method = sourceMethodName
                .take(15)
                .padEnd(15)
            val datetime = Instant
                .ofEpochMilli(millis)
                .let { LocalDateTime.ofInstant(it, ZoneId.systemDefault()) }
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            "${lvStr}[${className}#${method}]$datetime: ${message}\n"
        }
    }

    private fun formatClassName(className: String): String {
        val packageSections = className
            .split(".")
            .toMutableList()
        var size = className.length
        var index = 0
        var prefix = ""
        while (
            size > MAX_PACKAGE_SECTION_LENGTH && index < packageSections.size - 1
        ) {
            val section = packageSections[index]
            size -= section.length - 2
            packageSections[index] = section.take(1)
            index++
        }
        if (size > MAX_PACKAGE_SECTION_LENGTH) {
            size++
            prefix = "."
        }
        while (size > MAX_PACKAGE_SECTION_LENGTH && packageSections.size > 1) {
            packageSections.removeFirst()
            size -= 2
        }

        return packageSections
            .joinToString(".", prefix)
            .takeLast(MAX_PACKAGE_SECTION_LENGTH)
            .padStart(MAX_PACKAGE_SECTION_LENGTH)
    }
}