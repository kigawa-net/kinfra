package net.kigawa.kinfra.api.secret

import net.kigawa.kinfra.api.ctx.KinfraContext

interface SecretService {
    suspend fun getSecret(id: String): SecretResource
    suspend fun secretFile(id: String, ctx: KinfraContext): SecretFileResource
}