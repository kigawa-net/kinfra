package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.io.Reader

interface Hasher {
    suspend fun hash(
        string: String? = null, hash: List<HashValue> = listOf(), reader: Reader<String>? = null,
    ): HashValue
}