package net.kigawa.kinfra.model.service

import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kodel.api.err.ActionException
import net.kigawa.kodel.api.err.Res

/**
 * Terraformコマンドの実行を管理するサービス
 */
interface TerraformService {
    // ログイン前・kinfra.ktsにterraformブロックが無い場合はnullになる。CLIの起動時に
    // 例外を投げないよう、各Actionが必要に応じてnullチェックする。
    val terraformConfig: TerraformConfig?
    fun init(
        additionalArgs: List<String>,
        quiet: Boolean = false,
    ): Res<Int, ActionException>

    fun plan(
        additionalArgs: List<String>,
        quiet: Boolean = false,
        planFile: String? = null,
    ): Res<Int, ActionException>

    fun apply(
        planFile: String? = null,
        additionalArgs: List<String>,
        quiet: Boolean = false,
    ): Res<Int, ActionException>

    fun destroy(
        additionalArgs: List<String>,
        quiet: Boolean = false,
    ): Res<Int, ActionException>

    fun format(
        recursive: Boolean = true,
        quiet: Boolean = false,
    ): Res<Int, ActionException>

    fun validate(quiet: Boolean = false): Res<Int, ActionException>

    fun show(
        additionalArgs: List<String>,
        quiet: Boolean = false,
    ): Res<Int, ActionException>

}
