package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.deploy.KinfraContext

interface KinfraResource {
    fun hash(hasher: Hasher, ctx: KinfraContext): HashValue
}