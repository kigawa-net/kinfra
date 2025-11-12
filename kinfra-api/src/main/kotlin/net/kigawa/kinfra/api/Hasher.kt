package net.kigawa.kinfra.api

interface Hasher {
    fun hash(string: String, hash: List<HashValue> = listOf()): HashValue
}