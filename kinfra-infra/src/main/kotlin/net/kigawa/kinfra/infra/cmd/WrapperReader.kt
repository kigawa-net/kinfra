package net.kigawa.kinfra.infra.cmd

import net.kigawa.kinfra.api.io.Reader
import java.io.BufferedReader

class WrapperReader(val reader: BufferedReader): Reader<String> {
    override suspend fun read(): String {
        return reader.readLine()
    }

    override suspend fun hasNext(): Boolean {
        return reader.ready()
    }
}