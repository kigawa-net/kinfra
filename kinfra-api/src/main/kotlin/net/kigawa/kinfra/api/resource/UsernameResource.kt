package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

class UsernameResource(
    val strUsername: String,
): KinfraResource {
    init {
        require(strUsername.isNotBlank()) { "username is blank" }
    }

    override fun hash(
        hasher: Hasher, ctx: KinfraContext,
    ): HashValue {
        return hasher.hash(strUsername)
    }
}