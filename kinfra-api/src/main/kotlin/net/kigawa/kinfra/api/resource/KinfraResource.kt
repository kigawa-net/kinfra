package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher

interface KinfraResource {
    suspend fun hash(hasher: Hasher): HashValue
}