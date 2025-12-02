package net.kigawa.kodel.api.log

import java.io.OutputStream
import java.io.PrintStream

class LoggerStream(
    val kogger: Kogger,
    outputStream: OutputStream,
    val level: LogLevel,
): PrintStream(outputStream) {

    override fun println(x: String?) {
//        kogger.log(level.primary, x)
    }
}