package net.kigawa.kinfra.api.fs

interface FileReader: Reader<Char> {

    suspend fun <R> lineReader(block: suspend Reader<String>.() -> R): R
}