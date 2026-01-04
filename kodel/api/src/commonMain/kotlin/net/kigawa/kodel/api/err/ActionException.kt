package net.kigawa.kodel.api.err

/**
 * アクション実行時の例外。
 * 終了コードを保持する。
 *
 * @param exitCode 終了コード
 * @param message エラーメッセージ
 */
class ActionException(
    val exitCode: Int,
    message: String? = null,
) : Exception(message)
