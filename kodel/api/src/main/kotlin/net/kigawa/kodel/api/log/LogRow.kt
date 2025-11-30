package net.kigawa.kodel.api.log

data class LogRow(
    val message: String,
    val level: String,
    val time: String,
) {
}