package net.kigawa.kinfra.model.execution

import net.kigawa.kinfra.model.sub.SubProject
import java.io.File

/**
 * サブプロジェクトのうち、前回apply成功時から変更のあったものだけに絞り込む。
 * 具体的な実装（R2上のハッシュ保存等）はkinfra-infra層にあり、このインターフェースを
 * 通じてaction層から利用する。
 */
interface SubProjectChangeFilter {
    /**
     * [subProjects]（サブプロジェクトと解決済みディレクトリのペア）のうち、
     * 変更があった（または記録が無い＝初回の）ものだけを返す。
     */
    suspend fun filterChanged(subProjects: List<Pair<SubProject, File>>): List<Pair<SubProject, File>>

    /**
     * [subProject]のapplyが成功したことを受けて、現在のディレクトリ内容を記録する。
     */
    suspend fun recordSuccess(
        subProject: SubProject,
        dir: File,
    )

    companion object {
        /**
         * 変更検出を行わず、常に全件を「変更あり」として扱う実装
         * （R2の認証情報が揃っていない場合のfail-open用）。
         */
        val NOOP: SubProjectChangeFilter =
            object : SubProjectChangeFilter {
                override suspend fun filterChanged(subProjects: List<Pair<SubProject, File>>): List<Pair<SubProject, File>> =
                    subProjects

                override suspend fun recordSuccess(
                    subProject: SubProject,
                    dir: File,
                ) {
                }
            }
    }
}

/**
 * 解決済み（bws()マーカー解決済み）backendConfig/r2Bucket/r2Endpointから[SubProjectChangeFilter]を組み立てる。
 * 実装はkinfra-infra層にあり、DIコンテナで生成されてaction層に注入される。
 *
 * r2Bucket/r2EndpointがbackendConfigと別引数なのは、多くの利用側で各モジュールの`.tf`ファイルに
 * 既にbucket/endpointがハードコードされており、`-backend-config`として同じキーを渡すと
 * terraform initが二重定義エラーになるため（[net.kigawa.kinfra.model.conf.TerraformSettings.r2Bucket]参照）。
 */
interface SubProjectChangeFilterFactory {
    fun create(
        backendConfig: Map<String, String>,
        r2Bucket: String?,
        r2Endpoint: String?,
        parentProjectName: String,
    ): SubProjectChangeFilter
}
