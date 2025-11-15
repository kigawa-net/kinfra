package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext

class HostnameResource(
    val strHostname: String,
): KinfraResource {
    init {
        require(strHostname.isNotBlank()) {"hostname is blank"}
        require(!strHostname.contains("/")) {"hostname must not contain /"}
        require(!strHostname.contains(" ")) {"hostname must not contain space"}
        require(!strHostname.contains("\\")) {"hostname must not contain \\"}

    }
    override suspend fun hash(
        hasher: Hasher, ctx: KinfraContext,
    ): HashValue {
        return hasher.hash(strHostname)
    }
}