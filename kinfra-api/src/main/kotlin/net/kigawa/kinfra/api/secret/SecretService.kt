package net.kigawa.kinfra.api.secret

interface SecretService {
    suspend fun getSecret(id: String): SecretResource
}