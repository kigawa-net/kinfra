package net.kigawa.kinfra.infra.config.script

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

/**
 * kinfra.kts/kinfra-parent.kts共通のコンパイル設定。
 * スクリプトがDSLビルダークラス（TerraformConfigBuilder等）を解決できるよう、
 * 実行中のkinfra CLI自身のクラスパスをそのまま使う。
 */
object KinfraScriptCompilationConfiguration : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
})
