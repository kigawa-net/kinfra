package net.kigawa.kinfra.action

import net.kigawa.kinfra.model.CommandResult
import net.kigawa.kinfra.model.conf.TerraformConfig

/**
 * Terraformコマンドの実行を管理するサービス
 */
interface TerraformService {
    fun init(additionalArgs: Array<String> = emptyArray(), quiet: Boolean = true): CommandResult
    fun plan(additionalArgs: Array<String> = emptyArray(), quiet: Boolean = true): CommandResult
    fun apply(planFile: String? = null, additionalArgs: Array<String> = emptyArray(), quiet: Boolean = true): CommandResult
    fun destroy(additionalArgs: Array<String> = emptyArray(), quiet: Boolean = true): CommandResult
    fun format(recursive: Boolean = true, quiet: Boolean = true): CommandResult
    fun validate(quiet: Boolean = true): CommandResult
    fun show(additionalArgs: Array<String> = emptyArray(), quiet: Boolean = true): CommandResult
    fun getTerraformConfig(): TerraformConfig
}