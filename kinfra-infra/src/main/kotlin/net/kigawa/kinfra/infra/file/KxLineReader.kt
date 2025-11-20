package net.kigawa.kinfra.infra.file

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import net.kigawa.kinfra.api.io.Reader

class KxLineReader(
    private val source: RawSource
) : Reader<String> {

    private val buffer = Buffer()
    private var nextLine: String? = null
    private var eof = false

    override suspend fun hasNext(): Boolean {
        if (nextLine != null) return true
        if (eof) return false

        val sb = StringBuilder()

        while (true) {
            // If buffer is empty, try to fill it
            if (buffer.exhausted()) {
                val success = source.request(buffer, 1)
                if (!success) {
                    eof = true
                    if (sb.isNotEmpty()) {
                        nextLine = sb.toString()
                        return true
                    }
                    return false
                }
            }

            val c = buffer.readByte().toInt().toChar()

            if (c == '\n') {
                nextLine = sb.toString()
                return true
            }

            // ignore CR
            if (c != '\r') {
                sb.append(c)
            }
        }
    }

    override suspend fun read(): String {
        if (!hasNext()) throw NoSuchElementException()
        val line = nextLine!!
        nextLine = null
        return line
    }
}
