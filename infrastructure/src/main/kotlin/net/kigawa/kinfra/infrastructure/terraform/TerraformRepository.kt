package net.kigawa.kinfra.infrastructure.terraform

import net.kigawa.kinfra.model.conf.TerraformConfig
import net.kigawa.kinfra.infrastructure.file.FileRepository
import java.io.File

/**
 * Terraform設定の取得を担当するリポジトリ
 */
interface TerraformRepository {
    fun getTerraformConfig(): TerraformConfig
}

class TerraformRepositoryImpl(
    private val fileRepository: FileRepository
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

        // Terraformコンフィグファイル（.tfファイル）が存在するディレクトリを検索
        val terraformDir = findTerraformConfigDirectory(currentDir) ?: projectRoot

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

    /**
     * 現在のディレクトリから上位へさかのぼってTerraformコンフィグファイル（.tf）を探す
     */
    private fun findTerraformConfigDirectory(startDir: File): File? {
        var currentDir = startDir.absoluteFile
        
        while (currentDir != null) {
            // 現在のディレクトリに.tfファイルが存在するか確認
            val tfFiles = currentDir.listFiles { file ->
                file.isFile && file.name.endsWith(".tf")
            }
            
            if (!tfFiles.isNullOrEmpty()) {
                return currentDir
            }
            
            // 親ディレクトリへ移動（ルートディレクトリまで）
            currentDir = currentDir.parentFile
            
            // ルートディレクトリに到達したら終了
            if (currentDir != null && currentDir.parentFile == currentDir) {
                break
            }
        }
        
        return null
    }
}