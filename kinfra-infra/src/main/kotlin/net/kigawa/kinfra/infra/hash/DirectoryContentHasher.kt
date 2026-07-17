package net.kigawa.kinfra.infra.hash

import net.kigawa.kinfra.infra.Xxh3Hasher
import java.io.File

/**
 * サブプロジェクトディレクトリの内容を再帰的にハッシュ化する。
 * terraform実行によって生成される一時ファイル（state/plan/tfvars等）は
 * ソースの変更とは無関係なため除外する。
 */
object DirectoryContentHasher {
    private val EXCLUDED_DIR_NAMES = setOf(".terraform", ".git")
    private val EXCLUDED_FILE_NAMES = setOf("secrets.tfvars", "tfplan", "backend.tfvars")
    private val EXCLUDED_FILE_PREFIXES = listOf("terraform.tfstate")
    private val EXCLUDED_FILE_SUFFIXES = listOf(".tfplan")

    private fun isExcludedFile(file: File): Boolean {
        val name = file.name
        return name in EXCLUDED_FILE_NAMES ||
            EXCLUDED_FILE_PREFIXES.any { name.startsWith(it) } ||
            EXCLUDED_FILE_SUFFIXES.any { name.endsWith(it) }
    }

    /**
     * [dir]配下の対象ファイルをすべて相対パス順に並べ、
     * 「相対パス文字列」→「ファイル内容」の順でハッシュに反映して16進文字列を返す。
     * [dir]が存在しない場合は空文字列を返す。
     */
    suspend fun hash(dir: File): String {
        if (!dir.exists() || !dir.isDirectory) return ""

        val files =
            dir.walkTopDown()
                .onEnter { it.name !in EXCLUDED_DIR_NAMES }
                .filter { it.isFile && !isExcludedFile(it) }
                .map { it.relativeTo(dir).path to it }
                .sortedBy { it.first }
                .toList()

        val hasher = Xxh3Hasher()
        for ((relativePath, file) in files) {
            hasher.hash(relativePath)
            hasher.hash(file.readBytes())
        }
        val result = hasher.result()
        return "%016x".format(result.value)
    }
}
