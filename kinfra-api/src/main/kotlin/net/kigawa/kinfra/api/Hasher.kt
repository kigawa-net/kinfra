package net.kigawa.kinfra.api

import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.resource.KinfraResource

interface Hasher {
    suspend fun hash(
        str: List<String?> = listOf(),
        hash: List<HashValue> = listOf(),
        resource: List<KinfraResource> = listOf(),
        reader: Reader<String>? = null,
    ): HashValue
}