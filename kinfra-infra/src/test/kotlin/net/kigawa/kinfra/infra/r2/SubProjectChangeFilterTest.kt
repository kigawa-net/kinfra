package net.kigawa.kinfra.infra.r2

import kotlinx.coroutines.runBlocking
import net.kigawa.kinfra.model.execution.SubProjectChangeFilter
import net.kigawa.kinfra.model.sub.SubProject
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private data class FakeSubProject(
    override val name: String,
    override val path: String = name,
) : SubProject

private class FilterFakeR2ObjectStore : R2ObjectStore {
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

class SubProjectChangeFilterTest {
    private fun tempSubProjectDir(content: String): File {
        val dir = createTempDirectory().toFile()
        File(dir, "main.tf").writeText(content)
        return dir
    }

    private fun newFilter(): R2SubProjectChangeFilter =
        R2SubProjectChangeFilter(SubProjectHashStore(FilterFakeR2ObjectStore(), "infra", "hardware"))

    @Test
    fun firstRunTreatsEverythingAsChanged() =
        runBlocking {
            val filter = newFilter()
            val dir1 = tempSubProjectDir("resource \"a\" {}")
            val dir2 = tempSubProjectDir("resource \"b\" {}")
            try {
                val result = filter.filterChanged(listOf(FakeSubProject("k8s1") to dir1, FakeSubProject("alice") to dir2))

                assertEquals(2, result.size)
            } finally {
                dir1.deleteRecursively()
                dir2.deleteRecursively()
            }
        }

    @Test
    fun unchangedSubProjectIsSkippedAfterRecordSuccess() =
        runBlocking {
            val filter = newFilter()
            val dir = tempSubProjectDir("resource \"a\" {}")
            try {
                val subProject = FakeSubProject("k8s1")
                filter.recordSuccess(subProject, dir)

                val result = filter.filterChanged(listOf(subProject to dir))

                assertTrue(result.isEmpty())
            } finally {
                dir.deleteRecursively()
            }
        }

    @Test
    fun changedSubProjectIsIncludedAfterModification() =
        runBlocking {
            val filter = newFilter()
            val dir = tempSubProjectDir("resource \"a\" {}")
            try {
                val subProject = FakeSubProject("k8s1")
                filter.recordSuccess(subProject, dir)

                File(dir, "main.tf").writeText("resource \"a-modified\" {}")
                val result = filter.filterChanged(listOf(subProject to dir))

                assertEquals(1, result.size)
            } finally {
                dir.deleteRecursively()
            }
        }

    @Test
    fun onlyModifiedSubProjectIsIncludedWhenOthersAreUnchanged() =
        runBlocking {
            val filter = newFilter()
            val unchangedDir = tempSubProjectDir("resource \"a\" {}")
            val changedDir = tempSubProjectDir("resource \"b\" {}")
            try {
                val unchanged = FakeSubProject("k8s1")
                val changed = FakeSubProject("alice")
                filter.recordSuccess(unchanged, unchangedDir)
                filter.recordSuccess(changed, changedDir)
                File(changedDir, "main.tf").writeText("resource \"b-modified\" {}")

                val result = filter.filterChanged(listOf(unchanged to unchangedDir, changed to changedDir))

                assertEquals(listOf(changed to changedDir), result)
            } finally {
                unchangedDir.deleteRecursively()
                changedDir.deleteRecursively()
            }
        }

    @Test
    fun noopFailsOpenAndReturnsEverything() =
        runBlocking {
            val filter = SubProjectChangeFilter.NOOP
            val dir = tempSubProjectDir("resource \"a\" {}")
            try {
                val result = filter.filterChanged(listOf(FakeSubProject("k8s1") to dir))

                assertEquals(1, result.size)
            } finally {
                dir.deleteRecursively()
            }
        }

    @Test
    fun factoryFailsOpenWhenCredentialsMissing() {
        val filter = R2SubProjectChangeFilterFactory().create(mapOf("bucket" to "infra"), "hardware")

        runBlocking {
            val dir = tempSubProjectDir("resource \"a\" {}")
            try {
                val result = filter.filterChanged(listOf(FakeSubProject("k8s1") to dir))
                assertEquals(1, result.size)
            } finally {
                dir.deleteRecursively()
            }
        }
    }

    @Test
    fun factoryReturnsR2BackedFilterWhenCredentialsPresent() {
        // 実際のR2への接続はせず、fail-open(NOOP)ではないインスタンスが
        // 返ってくることだけを確認する
        val filter =
            R2SubProjectChangeFilterFactory().create(
                mapOf(
                    "bucket" to "infra",
                    "endpoint" to "https://example.r2.cloudflarestorage.com",
                    "access_key" to "ak",
                    "secret_key" to "sk",
                ),
                "hardware",
            )

        assertTrue(filter is R2SubProjectChangeFilter)
    }
}
