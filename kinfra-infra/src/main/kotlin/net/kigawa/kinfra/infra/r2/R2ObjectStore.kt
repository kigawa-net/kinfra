package net.kigawa.kinfra.infra.r2

/**
 * R2Clientのオブジェクト読み書き部分を切り出したインターフェース。
 * テスト時にフェイク実装へ差し替えられるようにする。
 */
interface R2ObjectStore {
    fun putObject(
        bucketName: String,
        key: String,
        data: ByteArray,
    )

    fun getObject(
        bucketName: String,
        key: String,
    ): ByteArray
}
