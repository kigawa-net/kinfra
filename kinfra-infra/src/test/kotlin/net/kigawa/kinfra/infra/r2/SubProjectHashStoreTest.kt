package net.kigawa.kinfra.infra.r2

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private class FakeR2ObjectStore : R2ObjectStore {
    val objects = mutableMapOf<String, ByteArray>()

    override fun putObject(
        bucketName: String,
        key: String,
        data: ByteArray,
    ) {
        objects["$bucketName/$key"] = data
    }

    override fun getObject(
        bucketName: String,
        key: String,
    ): ByteArray = objects["$bucketName/$key"] ?: throw NoSuchElementException("not found: $bucketName/$key")
}

class SubProjectHashStoreTest {
    @Test
    fun loadReturnsEmptyMapWhenNothingStoredYet() {
        val store = SubProjectHashStore(FakeR2ObjectStore(), "infra", "hardware")

        assertEquals(emptyMap(), store.load())
    }

    @Test
    fun saveThenLoadRoundTrips() {
        val fake = FakeR2ObjectStore()
        val store = SubProjectHashStore(fake, "infra", "hardware")

        store.save(mapOf("k8s1" to "abc123", "alice" to "def456"))

        assertEquals(mapOf("k8s1" to "abc123", "alice" to "def456"), store.load())
    }

    @Test
    fun updateOnlyChangesTheGivenSubProject() {
        val fake = FakeR2ObjectStore()
        val store = SubProjectHashStore(fake, "infra", "hardware")
        store.save(mapOf("k8s1" to "old-hash", "alice" to "unrelated-hash"))

        store.update("k8s1", "new-hash")

        assertEquals(mapOf("k8s1" to "new-hash", "alice" to "unrelated-hash"), store.load())
    }

    @Test
    fun differentParentProjectsUseDifferentKeys() {
        val fake = FakeR2ObjectStore()
        SubProjectHashStore(fake, "infra", "hardware").save(mapOf("k8s1" to "hardware-hash"))
        SubProjectHashStore(fake, "infra", "platform").save(mapOf("mcp-growi" to "platform-hash"))

        assertEquals(mapOf("k8s1" to "hardware-hash"), SubProjectHashStore(fake, "infra", "hardware").load())
        assertEquals(mapOf("mcp-growi" to "platform-hash"), SubProjectHashStore(fake, "infra", "platform").load())
    }

    @Test
    fun fakeStoreThrowsWhenObjectMissing() {
        assertFailsWith<NoSuchElementException> {
            FakeR2ObjectStore().getObject("infra", "missing.json")
        }
    }
}
