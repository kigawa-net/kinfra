package net.kigawa.kodel.api.err

/**
 * アクション実行時の例外
 * exitCodeを保持する
 */
class ActionException(
    val exitCode: Int,
    message: String? = null
) : Exception(message)
