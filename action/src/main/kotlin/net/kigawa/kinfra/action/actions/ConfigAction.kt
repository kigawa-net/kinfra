package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.FilePaths
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import kotlin.io.path.exists

class ConfigAction(
    private val loginRepo: LoginRepo,
    private val filePaths: FilePaths,
    private val configRepository: ConfigRepository,
): Action {

    override fun execute(args: Array<String>): Int {
        val isParentConfig = args.contains("--parent") || args.contains("-p")

        return if (isParentConfig) {
            showParentConfig()
        } else {
            showProjectConfig()
        }
    }

    private fun showProjectConfig(): Int {
        val configPath = try {
            loginRepo.kinfraConfigPath()
        } catch (e: IllegalStateException) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Login configuration not found")
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra login <github-repo>' first")
            return 1
        }

        val configFile = configPath.toFile()

        if (!configFile.exists()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Configuration file not found")
            println("${AnsiColors.CYAN}Expected location:${AnsiColors.RESET} ${configFile.absolutePath}")
            println()
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra config edit' to create a configuration file"
            )
            return 1
        }

        return displayConfigFile(configFile)
    }

    private fun showParentConfig(): Int {
        val config = loginRepo.loadKinfraParentConfig()
        if (config == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Parent configuration not found")
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra config edit --parent' to create a parent configuration file"
            )
            return 1
        }
        if (!config.filePath.exists()) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Parent configuration file not found")
            println("${AnsiColors.CYAN}Expected location:${AnsiColors.RESET} ${config.filePath}")
            println()
            println(
                "${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra config edit --parent' to create a parent configuration file"
            )
            return 1
        }

        return displayConfigFile(config.filePath.toFile())
    }

    private fun displayConfigFile(file: File): Int {
        return try {
            val content = file.readText()

            println("${AnsiColors.BLUE}Configuration:${AnsiColors.RESET} ${file.name}")
            println("${AnsiColors.CYAN}Location:${AnsiColors.RESET} ${file.absolutePath}")
            println()
            println(content)
            println()
            println("${AnsiColors.BLUE}Hint:${AnsiColors.RESET} Run 'kinfra config edit' to modify this configuration")
            0
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to read configuration file: ${e.message}")
            1
        }
    }

    override fun getDescription(): String {
        return "Display kinfra configuration (use --parent for parent config)"
    }

    override fun showHelp() {
        println("${AnsiColors.BLUE}Description:${AnsiColors.RESET}")
        println("  Display kinfra configuration files")
        println()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra config [options]")
        println()
        println("${AnsiColors.BLUE}Options:${AnsiColors.RESET}")
        println("  --parent, -p   Display parent project configuration (kinfra-parent.yaml)")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra config            Display project configuration")
        println("  kinfra config --parent   Display parent configuration")
        println()
        println("${AnsiColors.BLUE}Related Commands:${AnsiColors.RESET}")
        println("  kinfra config edit       Edit configuration files")
        println()
    }
}
