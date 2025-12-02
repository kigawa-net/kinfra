package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.io.Reader

interface FileReader: Reader<Char> {

    suspend fun <R> lineReader(block: suspend Reader<String>.() -> R): R
}