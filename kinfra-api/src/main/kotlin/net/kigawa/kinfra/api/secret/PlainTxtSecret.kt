package net.kigawa.kinfra.api.secret

import net.kigawa.kinfra.api.hash.HashSrc

class PlainTxtSecret(override val value: String): SecretResource {


    override suspend fun hashSrc(): HashSrc {
        return HashSrc.str(value)
    }
}