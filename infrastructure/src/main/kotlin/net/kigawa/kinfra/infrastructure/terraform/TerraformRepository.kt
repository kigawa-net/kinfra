package net.kigawa.kinfra.infrastructure.terraform

import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kinfra.model.conf.KinfraConfig
import net.kigawa.kinfra.infrastructure.file.FileRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import java.io.File
import java.nio.file.Paths

/**
 * Terraform設定の取得を担当するリポジトリ
 */
interface TerraformRepository {
    fun getTerraformConfig(): TerraformConfig?
}

class TerraformRepositoryImpl(
    private val fileRepository: FileRepository,
    private val configRepository: ConfigRepository,
    private val loginRepo: net.kigawa.kinfra.model.LoginRepo
) : TerraformRepository {

    override fun getTerraformConfig(): TerraformConfig? {
        // 設定ファイルからTerraform設定を読み込む
        val kinfraConfig = loginRepo.loadKinfraConfig()

        // kinfraConfigがnullの場合はTerraform設定なしとみなす
        if (kinfraConfig == null) {
            return null
        }

        // Terraformのワーキングディレクトリを決定
        val terraformDir = if (kinfraConfig.rootProject.terraform != null) {
            // 設定ファイルからworkingDirectoryを読み込む
            val workingDirPath = kinfraConfig.rootProject.terraform!!.workingDirectory
            if (workingDirPath.startsWith("/")) {
                // 絶対パス
                File(workingDirPath)
            } else {
                // 相対パス：プロジェクトルートからの相対パス
                File(loginRepo.repoPath.toFile(), workingDirPath)
            }
        } else {
            // Terraform設定がない場合はnullを返す
            return null
        }

        // tfvarsファイルの存在確認（terraformディレクトリ内を検索）
        val tfvarsFile = File(terraformDir, "terraform.tfvars")
        val varFile = if (fileRepository.exists(tfvarsFile)) tfvarsFile else null

        // SSH設定ファイルのパス（プロジェクトルートを基準）
        val sshConfigFile = File(loginRepo.repoPath.toFile(), "ssh_config")
        val sshConfigPath = fileRepository.getAbsolutePath(sshConfigFile)

        return TerraformConfig(
            workingDirectory = terraformDir,
            varFile = varFile,
            sshConfigPath = sshConfigPath,
            backendConfig = kinfraConfig.rootProject.terraform?.backendConfig ?: emptyMap()
        )
    }
}