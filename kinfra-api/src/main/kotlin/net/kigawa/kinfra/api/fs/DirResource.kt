package net.kigawa.kinfra.api.fs

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.KinfraResource

class DirResource(
    val dirPathResource: DirPathResource,
): KinfraResource {
    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(dirPathResource)
    }
}