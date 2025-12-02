package net.kigawa.kinfra.infra

import net.kigawa.kinfra.api.hash.HashValue
import net.kigawa.kinfra.api.hash.Hasher
import net.kigawa.kinfra.api.resource.KinfraResource
import net.openhft.hashing.LongHashFunction
import java.nio.ByteBuffer

class Xxh3Hasher(
    val bufferSize: Int = 1024,
): Hasher {
    val hasher = LongHashFunction.xx3()
    var buffer = ByteArray(0)
    var latest = 0L

    override suspend fun hash(str: String?) {
        hash(str?.toByteArray())
    }

    override suspend fun hash(bytes: ByteArray?) {
        buffer += bytes ?: byteArrayOf(Byte.MIN_VALUE)
        if (buffer.size < bufferSize) return
        latest = hasher.hashBytes(buffer)
        buffer = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(latest).array()
    }

    override suspend fun hash(resource: KinfraResource?) {
        if (resource == null) {
            hash(byteArrayOf(Byte.MIN_VALUE))
            return
        }
        resource.hashSrc().also { src ->
            src.strs.forEach { str -> hash(str) }
            src.resources.forEach { resource -> hash(resource) }
            src.blocks.forEach { block -> hash(block) }
        }
    }

    override suspend fun hash(block: suspend (Hasher) -> Unit) {
        block(this)
    }

    override suspend fun result(): HashValue {
        return HashValue(hasher.hashBytes(buffer))
    }
}