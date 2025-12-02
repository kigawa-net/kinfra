package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.hash.HashSrc

interface KinfraResource {
    suspend fun hashSrc(): HashSrc
}