package net.kigawa.kinfra.api.secret

import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.KinfraResource

interface SecretFileResource: KinfraResource {
    suspend fun asFileResource(): FileResource
}