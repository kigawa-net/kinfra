package net.kigawa.kinfra.infra.hash

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DirectoryContentHasherTest {
    @Test
    fun sameContentProducesSameHash() =
        runBlocking {
            val dir1 = createTempDirectory().toFile()
            val dir2 = createTempDirectory().toFile()
            try {
                File(dir1, "main.tf").writeText("resource \"x\" {}\n")
                File(dir2, "main.tf").writeText("resource \"x\" {}\n")

                val hash1 = DirectoryContentHasher.hash(dir1)
                val hash2 = DirectoryContentHasher.hash(dir2)

                assertEquals(hash1, hash2)
            } finally {
                dir1.deleteRecursively()
                dir2.deleteRecursively()
            }
        }

    @Test
    fun differentContentProducesDifferentHash() =
        runBlocking {
            val dir = createTempDirectory().toFile()
            try {
                val file = File(dir, "main.tf")
                file.writeText("resource \"x\" {}\n")
                val before = DirectoryContentHasher.hash(dir)

                file.writeText("resource \"y\" {}\n")
                val after = DirectoryContentHasher.hash(dir)

                assertNotEquals(before, after)
            } finally {
                dir.deleteRecursively()
            }
        }

    @Test
    fun excludedFilesAndDirsDoNotAffectHash() =
        runBlocking {
            val dir = createTempDirectory().toFile()
            try {
                File(dir, "main.tf").writeText("resource \"x\" {}\n")
                val before = DirectoryContentHasher.hash(dir)

                // 生成物・一時ファイルを追加してもハッシュは変わらないはず
                File(dir, "terraform.tfstate").writeText("{}")
                File(dir, "secrets.tfvars").writeText("secret = \"x\"")
                File(dir, "tfplan").writeText("binary-plan-data")
                File(dir, ".terraform").mkdirs()
                File(dir, ".terraform/provider.lock").writeText("lock")

                val after = DirectoryContentHasher.hash(dir)

                assertEquals(before, after)
            } finally {
                dir.deleteRecursively()
            }
        }

    @Test
    fun nonExistentDirectoryReturnsEmptyString() =
        runBlocking {
            val result = DirectoryContentHasher.hash(File("/nonexistent/path/that/should/not/exist"))
            assertEquals("", result)
        }
}
