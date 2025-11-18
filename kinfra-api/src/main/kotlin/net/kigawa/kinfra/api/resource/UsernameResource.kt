package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher

class UsernameResource(
    val strUsername: String,
): KinfraResource {
    init {
        require(strUsername.isNotBlank()) { "username is blank" }
    }

    override suspend fun hash(
        hasher: Hasher,
    ): HashValue {
        return hasher.hash(listOf(strUsername))
    }
}