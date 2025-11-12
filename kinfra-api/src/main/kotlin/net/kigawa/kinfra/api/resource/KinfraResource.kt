package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher

interface KinfraResource {
    fun hash(hasher: Hasher): HashValue
}