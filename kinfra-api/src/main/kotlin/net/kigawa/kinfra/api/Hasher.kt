package net.kigawa.kinfra.api

interface Hasher {
    fun hash(string: String): HashValue
}