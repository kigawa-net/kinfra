package net.kigawa.kinfra.api.hash

import net.kigawa.kinfra.api.resource.KinfraResource

interface Hasher {

    suspend fun hash(str: String?)
    suspend fun hash(bytes: ByteArray?)
    suspend fun hash(resource: KinfraResource?)
    suspend fun hash(block: suspend (Hasher) -> Unit)
    suspend fun result(): HashValue
}