package net.kigawa.kinfra.action

import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kinfra.model.err.ActionException
import net.kigawa.kinfra.model.err.Res

/**
 * Terraformコマンドの実行を管理するサービス
 */
interface TerraformService {
    fun init(additionalArgs: List<String> , quiet: Boolean = true): Res<Int, ActionException>
    fun plan(additionalArgs: List<String>, quiet: Boolean = true): Res<Int, ActionException>
    fun apply(planFile: String? = null, additionalArgs: List<String>, quiet: Boolean = true): Res<Int, ActionException>
    fun destroy(additionalArgs: List<String>, quiet: Boolean = true): Res<Int, ActionException>
    fun format(recursive: Boolean = true, quiet: Boolean = true): Res<Int, ActionException>
    fun validate(quiet: Boolean = true): Res<Int, ActionException>
    fun show(additionalArgs: List<String>, quiet: Boolean = true): Res<Int, ActionException>
    fun getTerraformConfig(): TerraformConfig
}