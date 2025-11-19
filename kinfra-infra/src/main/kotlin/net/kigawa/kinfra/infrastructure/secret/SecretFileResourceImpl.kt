package net.kigawa.kinfra.infrastructure.secret

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kinfra.model.BitwardenSecret

class SecretFileResourceImpl(
    val secret: BitwardenSecret,
): SecretFileResource {
    override suspend fun hash(hasher: Hasher): HashValue {
        return secret.hash(hasher)
    }
}