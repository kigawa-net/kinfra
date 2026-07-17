package net.kigawa.kinfra.infra.config.script

import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

/**
 * kinfra.kts/kinfra-parent.ktsを実行時に評価するための共通ホスト。
 */
object ScriptHost {
    @PublishedApi
    internal val host = BasicJvmScriptingHost()

    /**
     * [file]を[T]のスクリプトテンプレートとして評価し、評価済みのスクリプトインスタンスを返す。
     * 評価に失敗した場合は例外を投げる（呼び出し側で必要に応じてキャッチする）。
     */
    inline fun <reified T : Any> eval(file: File): T {
        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()
        val result =
            host.eval(
                file.toScriptSource(),
                compilationConfiguration,
                ScriptEvaluationConfiguration {},
            )

        return when (result) {
            is ResultWithDiagnostics.Success -> {
                result.value.returnValue.scriptInstance as? T
                    ?: throw IllegalStateException("Failed to evaluate script as ${T::class.simpleName}: ${file.absolutePath}")
            }

            is ResultWithDiagnostics.Failure -> {
                val messages = result.reports.joinToString("\n") { it.render() }
                throw IllegalArgumentException("Failed to parse config script ${file.absolutePath}:\n$messages")
            }
        }
    }

    fun ScriptDiagnostic.render(): String = "${severity.name}: $message" + (location?.let { " ($it)" } ?: "")
}
