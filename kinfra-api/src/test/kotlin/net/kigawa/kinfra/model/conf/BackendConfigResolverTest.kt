package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.BitwardenSecret
import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository
import kotlin.test.Test
import kotlin.test.assertEquals

private class FakeBitwardenSecretManagerRepository(
    private val byId: Map<String, BitwardenSecret>,
    private val byKey: Map<String, BitwardenSecret>,
) : BitwardenSecretManagerRepository {
    override fun listSecrets(): List<BitwardenSecret> = (byId.values + byKey.values).distinct()

    override fun getSecret(id: String): BitwardenSecret? = byId[id]

    override fun findSecretByKey(key: String): BitwardenSecret? = byKey[key]

    override fun close() {}
}

private fun secret(
    id: String,
    key: String,
    value: String,
): BitwardenSecret =
    BitwardenSecret(
        id = id,
        organizationId = "org",
        projectId = null,
        key = key,
        value = value,
        note = "",
        creationDate = "",
        revisionDate = "",
    )

class BackendConfigResolverTest {
    @Test
    fun resolvesUuidShapedSecretKeyById() {
        val uuid = "eb5eb0e8-2a4a-4398-a756-b37000d87d64"
        val repo =
            FakeBitwardenSecretManagerRepository(
                byId = mapOf(uuid to secret(uuid, "unrelated-name", "id-resolved-value")),
                byKey = emptyMap(),
            )

        val resolved = BackendConfigResolver.resolveSecret(repo, uuid)

        assertEquals("id-resolved-value", resolved)
    }

    @Test
    fun resolvesNonUuidSecretKeyByName() {
        val repo =
            FakeBitwardenSecretManagerRepository(
                byId = emptyMap(),
                byKey = mapOf("r2-access-key-id" to secret("some-id", "r2-access-key-id", "name-resolved-value")),
            )

        val resolved = BackendConfigResolver.resolveSecret(repo, "r2-access-key-id")

        assertEquals("name-resolved-value", resolved)
    }

    @Test
    fun flattenAndResolveMergesNestedMapsAndResolvesBwsMarkers() {
        val uuid = "c39086cc-e112-40eb-b19f-b37000d89090"
        val repo =
            FakeBitwardenSecretManagerRepository(
                byId = mapOf(uuid to secret(uuid, "unused", "resolved-secret")),
                byKey = emptyMap(),
            )

        val backendConfig =
            mapOf(
                "bucket" to "infra",
                "config" to
                    mapOf(
                        "secret_key" to BwsMarker.wrap(uuid),
                        "region" to "auto",
                    ),
            )

        val result = BackendConfigResolver.flattenAndResolve(backendConfig, repo)

        assertEquals("infra", result["bucket"])
        assertEquals("resolved-secret", result["config.secret_key"])
        assertEquals("auto", result["config.region"])
    }

    @Test
    fun flattenAndResolveKeepsMarkerLiteralWhenNoRepositoryAvailable() {
        val backendConfig = mapOf("access_key" to BwsMarker.wrap("some-key"))

        val result = BackendConfigResolver.flattenAndResolve(backendConfig, null)

        assertEquals(BwsMarker.wrap("some-key"), result["access_key"])
    }
}
