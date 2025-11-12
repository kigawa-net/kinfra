package net.kigawa.kinfra.api

interface Hasher {
    fun hash(string: String, vararg hash: HashValue): HashValue
}