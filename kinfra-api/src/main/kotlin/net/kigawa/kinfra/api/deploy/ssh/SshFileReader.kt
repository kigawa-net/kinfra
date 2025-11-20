package net.kigawa.kinfra.api.deploy.ssh

import net.kigawa.kinfra.api.io.FileReader
import net.kigawa.kinfra.api.io.Reader

class SshFileReader(val reader: Reader<String>): FileReader {
    var last = mutableListOf<Char>()

    override suspend fun <R> lineReader(block: suspend Reader<String>.() -> R): R {
        return reader.block()

    }

    override suspend fun read(): Char {
        if (last.isEmpty()) {
            last = reader.read().toMutableList().apply { add('\n') }
        }
        return last.removeFirst()
    }

    override suspend fun hasNext(): Boolean {
        return last.isNotEmpty() || reader.hasNext()
    }
}