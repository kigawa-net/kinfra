package net.kigawa.kinfra.infra.r2

import kotlinx.serialization.json.Json

/**
 * サブプロジェクトごとの「最後にapplyが成功した時点のディレクトリハッシュ」を
 * R2上のJSONオブジェクトとして読み書きする。実際のterraform state（`<module>/terraform.tfstate`
 * 等）とキーが衝突しないよう、専用のprefixを使う。
 */
class SubProjectHashStore(
    private val store: R2ObjectStore,
    private val bucketName: String,
    parentProjectName: String,
) {
    private val key = "_kinfra/subproject-hashes/$parentProjectName.json"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 保存済みのハッシュ一覧を読み込む。オブジェクトが存在しない場合や読み込みに失敗した場合は
     * 空のMapを返す（初回実行として扱う）。
     */
    fun load(): Map<String, String> {
        return try {
            val bytes = store.getObject(bucketName, key)
            json.decodeFromString<Map<String, String>>(bytes.decodeToString())
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun save(hashes: Map<String, String>) {
        store.putObject(bucketName, key, json.encodeToString(hashes).toByteArray())
    }

    /**
     * 1件のサブプロジェクトのハッシュだけを更新して保存する
     * （既存の保存済みハッシュ一覧に対して該当エントリだけを上書き/追加する）。
     */
    fun update(
        subProjectName: String,
        hash: String,
    ) {
        val current = load().toMutableMap()
        current[subProjectName] = hash
        save(current)
    }
}
