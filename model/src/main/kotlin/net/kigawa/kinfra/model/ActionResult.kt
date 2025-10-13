package net.kigawa.kinfra.model

/**
 * コマンド実行結果を表すドメインモデル
 */
data class ActionResult(
    val exitCode: Int,
    val message: String? = null
) {
    val isSuccess: Boolean
        get() = exitCode == 0

    val isFailure: Boolean
        get() = exitCode != 0

    companion object {
        fun failure(exitCode: Int = 1, message: String? = null) = ActionResult(exitCode, message)
    }
}