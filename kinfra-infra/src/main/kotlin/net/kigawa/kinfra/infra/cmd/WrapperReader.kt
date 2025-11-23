package net.kigawa.kinfra.infra.cmd

import net.kigawa.kinfra.api.io.Reader
import java.io.BufferedReader

class WrapperReader(val reader: BufferedReader): Reader<String> {
    override suspend fun read(): String? {
        if (reader.ready()) return null
        return reader.readLine()
    }
}