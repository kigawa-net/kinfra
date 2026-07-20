package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc

class UsernameResource(
    val strUsername: String,
): KinfraResource {
    init {
        require(strUsername.isNotBlank()) { "username is blank" }
    }


    override suspend fun hashSrc(): HashSrc {
        return HashSrc.str(strUsername)
    }
}