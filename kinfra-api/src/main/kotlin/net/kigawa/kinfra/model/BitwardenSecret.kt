package net.kigawa.kinfra.model

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.secret.SecretResource

/**
 * Bitwarden Secret Manager のシークレット
 */
data class BitwardenSecret(
    val id: String,
    val organizationId: String,
    val projectId: String?,
    val key: String,
    val value: String,
    val note: String,
    val creationDate: String,
    val revisionDate: String,
): SecretResource {
    override suspend fun hash(hasher: Hasher): HashValue {
        return hasher.hash(str = listOf(
            id,
            organizationId,
            projectId,
            key,
            value,
            note,
            creationDate,
            revisionDate
        ))
    }
}
