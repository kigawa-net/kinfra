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

    override suspend fun <U, R> map(
        translate: suspend (Char) -> U, block: suspend Reader<U>.() -> R,
    ): R {
        return reader.flatMap({ it.map { char -> translate(char) } }, block)
    }

    override suspend fun <U, R> flatMap(
        translate: suspend (Char) -> List<U>,
        block: suspend (Reader<U>) -> R,
    ): R {
        return reader.flatMap({ it.flatMap { char -> translate(char) } }, block)
    }


}