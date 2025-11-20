package net.kigawa.kinfra.model

import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.secret.SecretResource

/**
 * Bitwarden Secret Manager のシークレット
 */
data class BitwardenSecret(
    val id: String,
    val organizationId: String,
    val projectId: String?,
    val key: String,
    override val value: String,
    val note: String,
    val creationDate: String,
    val revisionDate: String,
): SecretResource {

    override suspend fun hashSrc() = HashSrc.str(
        id,
        organizationId,
        projectId,
        key,
        value,
        note,
        creationDate,
        revisionDate
    )


}
