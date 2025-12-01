package net.kigawa.kinfra.infra.file

import kotlinx.io.RawSink
import kotlinx.io.buffered
import kotlinx.io.writeString
import net.kigawa.kinfra.api.io.Writer

class FileLineWriter(source: RawSink): Writer {
    val bufferedSink = source.buffered()
    override fun write(str: String) {
        bufferedSink.writeString(str)
        bufferedSink.writeString("\n")
        bufferedSink.flush()
    }
}
