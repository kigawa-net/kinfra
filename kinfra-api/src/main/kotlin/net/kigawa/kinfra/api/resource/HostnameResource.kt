package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc

class HostnameResource(
    val strHostname: String,
): KinfraResource {
    init {
        require(strHostname.isNotBlank()) { "hostname is blank" }
        require(!strHostname.contains("/")) { "hostname must not contain /" }
        require(!strHostname.contains(" ")) { "hostname must not contain space" }
        require(!strHostname.contains("\\")) { "hostname must not contain \\" }

    }

    override suspend fun hashSrc(): HashSrc {
        return HashSrc.str(strHostname)
    }
}