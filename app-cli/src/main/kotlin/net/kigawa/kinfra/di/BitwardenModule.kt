package net.kigawa.kinfra.di

import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.infrastructure.bitwarden.BitwardenSecretManagerRepositoryImpl
import net.kigawa.kinfra.infrastructure.file.SystemHomeDirGetter
import net.kigawa.kinfra.model.conf.FilePaths
import org.koin.dsl.module

val bitwardenModule = module {
    // Bitwarden Secret Manager (環境変数または .bws_token ファイルから BWS_ACCESS_TOKEN を取得)
    // Note: FilePaths は既に登録されているので、早期に取得可能
    val filePathsInstance = FilePaths(SystemHomeDirGetter())
    val bwsAccessToken = System.getenv("BWS_ACCESS_TOKEN")?.also {
        println("✓ Using BWS_ACCESS_TOKEN from environment variable")
    } ?: run {
        // ファイルから読み込み
        val tokenFile = java.io.File(filePathsInstance.bwsTokenFileName)
        if (tokenFile.exists() && tokenFile.canRead()) {
            tokenFile.readText().trim().takeIf { it.isNotBlank() }?.also {
                println("✓ Loaded BWS_ACCESS_TOKEN from .bws_token file")
            }
        } else {
            null
        }
    }
    val hasBwsToken = bwsAccessToken != null && bwsAccessToken.isNotBlank()

    if (!hasBwsToken) {
        println("⚠ BWS_ACCESS_TOKEN not available - SDK commands will not be registered")
    }

    if (hasBwsToken) {
        single<BitwardenSecretManagerRepository>(createdAtStart = true) {
            // .env から BW_PROJECT を読み込む
            val envFileLoader = get<EnvFileLoader>()
            val projectId = envFileLoader.get("BW_PROJECT")
            BitwardenSecretManagerRepositoryImpl(bwsAccessToken, get(), projectId)
        }
    }
}