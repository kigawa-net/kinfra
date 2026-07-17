package net.kigawa.kinfra.infra.r2

import net.kigawa.kinfra.infra.hash.DirectoryContentHasher
import net.kigawa.kinfra.model.execution.SubProjectChangeFilter
import net.kigawa.kinfra.model.execution.SubProjectChangeFilterFactory
import net.kigawa.kinfra.model.sub.SubProject
import java.io.File

/**
 * [SubProjectChangeFilter]のR2バックエンド実装。
 * サブプロジェクトのディレクトリ内容ハッシュを、R2に保存された前回apply成功時の
 * ハッシュと比較する（[SubProjectHashStore]/[DirectoryContentHasher]参照）。
 */
class R2SubProjectChangeFilter(
    private val hashStore: SubProjectHashStore,
) : SubProjectChangeFilter {
    override suspend fun filterChanged(subProjects: List<Pair<SubProject, File>>): List<Pair<SubProject, File>> {
        val previousHashes = hashStore.load()
        return subProjects.filter { (subProject, dir) ->
            val currentHash = DirectoryContentHasher.hash(dir)
            currentHash != previousHashes[subProject.name]
        }
    }

    override suspend fun recordSuccess(
        subProject: SubProject,
        dir: File,
    ) {
        hashStore.update(subProject.name, DirectoryContentHasher.hash(dir))
    }
}

/**
 * 解決済みbackendConfigから[R2SubProjectChangeFilter]、または必要な値が
 * 揃っていない場合は[SubProjectChangeFilter.NOOP]（fail-open）を組み立てる。
 */
class R2SubProjectChangeFilterFactory : SubProjectChangeFilterFactory {
    override fun create(
        backendConfig: Map<String, String>,
        r2Bucket: String?,
        r2Endpoint: String?,
        parentProjectName: String,
    ): SubProjectChangeFilter {
        val accessKey = backendConfig["access_key"]
        val secretKey = backendConfig["secret_key"]

        if (r2Bucket == null || r2Endpoint == null || accessKey == null || secretKey == null) {
            return SubProjectChangeFilter.NOOP
        }

        val r2Client = R2Client(r2Endpoint, accessKey, secretKey)
        return R2SubProjectChangeFilter(SubProjectHashStore(r2Client, r2Bucket, parentProjectName))
    }
}
