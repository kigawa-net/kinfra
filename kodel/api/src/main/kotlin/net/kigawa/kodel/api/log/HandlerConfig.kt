package net.kigawa.kodel.api.log

data class HandlerConfig(
    var formatter: (LogRow) -> String = { row -> "${row.time} ${row.level} ${row.message}" },
) {
    lateinit var level: LogLevel
}
