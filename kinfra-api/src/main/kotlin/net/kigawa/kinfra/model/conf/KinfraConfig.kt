package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.conf.global.LoginConfig

interface KinfraConfig {
    val rootProject: ProjectInfo
    val bitwarden: BitwardenSettings?
    val subProjects: List<ProjectInfo>
    val update: UpdateSettings?

    @Deprecated("Login configuration should be in GlobalConfig. This property is kept for backward compatibility.")
    val login: LoginConfig?
}

interface ProjectInfo {
    val projectId: String
    val description: String?
    val terraform: TerraformSettings?
}

interface TerraformVariableMapping {
    val terraformVariable: String
    val bitwardenSecretKey: String
}

interface TerraformOutputMapping {
    val terraformOutput: String
    val bitwardenSecretKey: String
}

interface TerraformSettings {
    val version: String
    val workingDirectory: String
    val variableMappings: List<TerraformVariableMapping>
        get() = emptyList()
    val outputMappings: List<TerraformOutputMapping>
        get() = emptyList()
    /**
     * -backend-configとしてterraformに渡すキーバリュー。値はkinfra.kts/kinfra-parent.ktsの
     * `bws("secret-key")`で参照されたBitwardenシークレットのマーカーを含み得る
     * （[BwsMarker]参照）。実際にterraformを呼び出す直前に[BackendConfigResolver]で解決される。
     */
    val backendConfig: Map<String, String>
        get() = emptyMap()
    val generateOutputDir: String?
        get() = null
    /**
     * サブプロジェクトの変更検出（[net.kigawa.kinfra.model.execution.SubProjectChangeFilter]）が
     * ハッシュキャッシュの読み書きに使うR2バケット名/エンドポイント。
     * これらは各モジュールの`.tf`ファイル側backendブロックに既にハードコードされていることが多く、
     * `backendConfig`（-backend-config引数）に含めるとterraform initが二重定義エラーになるため、
     * 意図的に別フィールドとして分離している。値は[BwsMarker]でラップされ得る。
     */
    val r2Bucket: String?
        get() = null
    val r2Endpoint: String?
        get() = null
}

interface BitwardenSettings {
    val projectId: String
}

interface UpdateSettings {
    val autoUpdate: Boolean
    val checkInterval: Long
    val githubRepo: String
}
