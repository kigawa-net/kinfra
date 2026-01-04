package net.kigawa.kodel.api.log.config.formatter

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import net.kigawa.kodel.api.log.LogRow

object DefaultFormatter: LoggerFormatter {
    const val MAX_PACKAGE_SECTION_LENGTH = 40

    override fun format(row: LogRow): String {
        return row.run {
            val lvStr = level.name.padEnd(8)
            val className = formatClassName(sourceClassName)
            val method = sourceMethodName
                .take(15)
                .padEnd(15)

            val datetime = localDatetime.format(LocalDateTime.Format {
                year()
                char('-')
                monthNumber()
                char('-')
                day()
                char(' ')
                hour()
                char(':')
                minute()
                char(':')
                second()
            })
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
