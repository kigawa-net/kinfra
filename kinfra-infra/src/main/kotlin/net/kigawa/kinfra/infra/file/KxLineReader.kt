package net.kigawa.kinfra.infra.file

import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.readLine
import net.kigawa.kinfra.api.io.Reader

class KxLineReader(
    source: RawSource,
): Reader<String> {
    val bufferedSource = source.buffered()


    override suspend fun read(): String? {
        return bufferedSource.readLine()
    }
}
