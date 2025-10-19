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
    fun getTerraformConfig(): TerraformConfig
}

class TerraformRepositoryImpl(
    private val fileRepository: FileRepository,
    private val configRepository: ConfigRepository
) : TerraformRepository {

    override fun getTerraformConfig(): TerraformConfig {
        // プロジェクトルートを特定
        // Gradleから実行された場合、user.dirはappディレクトリを指すので親に移動
        val currentDir = File(System.getProperty("user.dir"))
        val projectRoot = if (currentDir.name == "app") {
            currentDir.parentFile
        } else {
            currentDir
        }

        // 設定ファイルからTerraform設定を読み込む
        val configPath = configRepository.getProjectConfigFilePath()
        val kinfraConfig = configRepository.loadKinfraConfig(Paths.get(configPath))
        
        // Terraformのワーキングディレクトリを決定
        val terraformDir = if (kinfraConfig != null) {
            // 設定ファイルからworkingDirectoryを読み込む
            val workingDirPath = kinfraConfig.terraform.workingDirectory
            if (workingDirPath.startsWith("/")) {
                // 絶対パス
                File(workingDirPath)
            } else {
                // 相対パス：プロジェクトルートからの相対パス
                File(projectRoot, workingDirPath)
            }
        } else {
            // 設定ファイルがない場合は現在のディレクトリを使用
            currentDir
        }

        // tfvarsファイルの存在確認（terraformディレクトリ内を検索）
        val tfvarsFile = File(terraformDir, "terraform.tfvars")
        val varFile = if (fileRepository.exists(tfvarsFile)) tfvarsFile else null

        // SSH設定ファイルのパス（プロジェクトルートを基準）
        val sshConfigFile = File(projectRoot, "ssh_config")
        val sshConfigPath = fileRepository.getAbsolutePath(sshConfigFile)

        return TerraformConfig(
            workingDirectory = terraformDir,
            varFile = varFile,
            sshConfigPath = sshConfigPath
        )
    }
}