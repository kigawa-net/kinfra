package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.bitwarden.BitwardenSecretManagerRepository

/**
 * backendConfig（terraformの-backend-config相当）をフラットなキーバリューペアに変換し、
 * `bws(...)`マーカーが含まれる値をBitwarden Secret Managerから解決する共通ロジック。
 *
 * TerraformServiceImpl（トップレベルのinit/plan/apply/destroy/show）と、
 * CurrentPlanAction/CurrentApplyAction/SubPlanAction/SubApplyAction
 * （kinfra login不要で任意のディレクトリに対して実行するコマンド群）の両方から使われる。
 */
object BackendConfigResolver {
    private val UUID_REGEX = Regex("^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$")

    /**
     * secretKeyがBitwarden Secret ManagerのUUID形式であればID指定で、
     * そうでなければキー名指定でシークレットを検索する。
     */
    fun resolveSecret(
        secretManager: BitwardenSecretManagerRepository?,
        secretKey: String,
    ): String? {
        if (secretManager == null) return null
        return if (UUID_REGEX.matches(secretKey)) {
            secretManager.getSecret(secretKey)?.value
        } else {
            secretManager.findSecretByKey(secretKey)?.value
        }
    }

    /**
     * 単一のnullable String値（r2Bucket/r2Endpoint等、backendConfigのMapに属さない値）に
     * 含まれ得る`bws(...)`マーカーを解決する。
     */
    fun resolveValue(
        value: String?,
        secretManager: BitwardenSecretManagerRepository?,
    ): String? {
        if (value == null) return null
        return BwsMarker.resolve(value) { resolveSecret(secretManager, it) }
    }

    fun flattenAndResolve(
        backendConfig: Map<String, Any>,
        secretManager: BitwardenSecretManagerRepository?,
        prefix: String = "",
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()

        backendConfig.forEach { (key, value) ->
            val fullKey = if (prefix.isEmpty()) key else "$prefix.$key"

            when (value) {
                is String -> result[fullKey] = BwsMarker.resolve(value) { resolveSecret(secretManager, it) }
                is Number -> result[fullKey] = value.toString()
                is Boolean -> result[fullKey] = value.toString()
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    val nestedMap = value as Map<String, Any>
                    result.putAll(flattenAndResolve(nestedMap, secretManager, fullKey))
                }

                else -> result[fullKey] = value.toString()
            }
        }

        return result
    }
}
