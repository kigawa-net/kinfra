package net.kigawa.kinfra.commands

import net.kigawa.kinfra.infrastructure.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformVarsManager
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.util.AnsiColors

class HelloCommand(
    private val configRepository: ConfigRepository,
    private val terraformVarsManager: TerraformVarsManager,
    private val processExecutor: ProcessExecutor,
    private val logger: Logger
) : Command {
    override fun execute(args: Array<String>): Int {
        println("${AnsiColors.CYAN}${AnsiColors.BOLD}Welcome to kinfra interactive manager!${AnsiColors.RESET}")
        println()

        while (true) {
            showMainMenu()
            print("${AnsiColors.GREEN}Select an option (1-5):${AnsiColors.RESET} ")
            val choice = readLine()?.trim() ?: ""

            println()
            when (choice) {
                "1" -> listHosts()
                "2" -> enableHost()
                "3" -> disableHost()
                "4" -> gitStatus()
                "5" -> gitPush()
                "0", "q", "quit", "exit" -> {
                    println("${AnsiColors.CYAN}Goodbye!${AnsiColors.RESET}")
                    return 0
                }
                else -> println("${AnsiColors.RED}Invalid option. Please try again.${AnsiColors.RESET}")
            }
            println()
        }
    }

    override fun getDescription(): String {
        return "Interactive management tool for Terraform directories and Git operations"
    }

    override fun requiresEnvironment(): Boolean {
        return false
    }

    private fun showMainMenu() {
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}=== Main Menu ===${AnsiColors.RESET}")
        println("  ${AnsiColors.CYAN}1.${AnsiColors.RESET} List Terraform directories")
        println("  ${AnsiColors.CYAN}2.${AnsiColors.RESET} Enable Terraform directory")
        println("  ${AnsiColors.CYAN}3.${AnsiColors.RESET} Disable Terraform directory")
        println("  ${AnsiColors.CYAN}4.${AnsiColors.RESET} Check Git status")
        println("  ${AnsiColors.CYAN}5.${AnsiColors.RESET} Push to Git repository")
        println("  ${AnsiColors.CYAN}0.${AnsiColors.RESET} Exit")
        println()
    }

    private fun listHosts() {
        logger.info("Listing hosts")
        val config = configRepository.loadHostsConfig()
        val configPath = configRepository.getConfigFilePath()

        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Terraform Directories${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Config file: $configPath${AnsiColors.RESET}")
        println()

        val hosts = mutableListOf<Triple<String, Boolean, String>>()
        HostsConfig.DEFAULT_HOSTS.keys.forEach { hostName ->
            val enabled = config.hosts[hostName] ?: HostsConfig.DEFAULT_HOSTS[hostName] ?: false
            val description = HostsConfig.HOST_DESCRIPTIONS[hostName] ?: "No description"
            hosts.add(Triple(hostName, enabled, description))
        }

        hosts.forEach { (name, enabled, description) ->
            val status = if (enabled) "${AnsiColors.GREEN}enabled${AnsiColors.RESET}" else "${AnsiColors.YELLOW}disabled${AnsiColors.RESET}"
            println("  ${name.padEnd(15)} [$status]  $description")
        }
    }

    private fun enableHost() {
        logger.info("Enabling host")
        println("${AnsiColors.BLUE}Available directories:${AnsiColors.RESET}")
        HostsConfig.DEFAULT_HOSTS.keys.forEachIndexed { index, host ->
            println("  ${index + 1}. $host")
        }
        println()

        print("${AnsiColors.GREEN}Enter directory name or number:${AnsiColors.RESET} ")
        val input = readLine()?.trim() ?: ""

        val hostName = input.toIntOrNull()?.let { number ->
            HostsConfig.DEFAULT_HOSTS.keys.toList().getOrNull(number - 1)
        } ?: input

        if (!HostsConfig.DEFAULT_HOSTS.containsKey(hostName)) {
            println("${AnsiColors.RED}Error: Unknown directory '$hostName'${AnsiColors.RESET}")
            return
        }

        val config = configRepository.loadHostsConfig()
        val updatedHosts = config.hosts.toMutableMap()
        updatedHosts[hostName] = true

        configRepository.saveHostsConfig(HostsConfig(updatedHosts))
        logger.info("Host $hostName enabled")

        terraformVarsManager.updateHostsVars()
        val varsPath = terraformVarsManager.getVarsFilePath()
        logger.info("Updated Terraform vars file: $varsPath")

        println("${AnsiColors.GREEN}✓ Directory '$hostName' has been enabled${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Updated Terraform vars: $varsPath${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Note: Run 'init' and 'apply' to apply changes${AnsiColors.RESET}")
    }

    private fun disableHost() {
        logger.info("Disabling host")
        println("${AnsiColors.BLUE}Available directories:${AnsiColors.RESET}")
        HostsConfig.DEFAULT_HOSTS.keys.forEachIndexed { index, host ->
            println("  ${index + 1}. $host")
        }
        println()

        print("${AnsiColors.GREEN}Enter directory name or number:${AnsiColors.RESET} ")
        val input = readLine()?.trim() ?: ""

        val hostName = input.toIntOrNull()?.let { number ->
            HostsConfig.DEFAULT_HOSTS.keys.toList().getOrNull(number - 1)
        } ?: input

        if (!HostsConfig.DEFAULT_HOSTS.containsKey(hostName)) {
            println("${AnsiColors.RED}Error: Unknown directory '$hostName'${AnsiColors.RESET}")
            return
        }

        val config = configRepository.loadHostsConfig()
        val updatedHosts = config.hosts.toMutableMap()
        updatedHosts[hostName] = false

        configRepository.saveHostsConfig(HostsConfig(updatedHosts))
        logger.info("Host $hostName disabled")

        terraformVarsManager.updateHostsVars()
        val varsPath = terraformVarsManager.getVarsFilePath()
        logger.info("Updated Terraform vars file: $varsPath")

        println("${AnsiColors.GREEN}✓ Directory '$hostName' has been disabled${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Updated Terraform vars: $varsPath${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Note: Run 'init' and 'apply' to apply changes${AnsiColors.RESET}")
    }

    private fun gitStatus() {
        logger.info("Checking git status")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Git Status${AnsiColors.RESET}")
        println()

        val result = processExecutor.executeWithOutput(arrayOf("git", "status"))

        if (result.exitCode == 0) {
            println(result.output)
        } else {
            println("${AnsiColors.RED}Error checking git status:${AnsiColors.RESET}")
            println(result.error)
        }
    }

    private fun gitPush() {
        logger.info("Pushing to git repository")

        // まず、現在のブランチを確認
        val branchResult = processExecutor.executeWithOutput(arrayOf("git", "branch", "--show-current"))
        if (branchResult.exitCode != 0) {
            println("${AnsiColors.RED}Error: Could not determine current branch${AnsiColors.RESET}")
            return
        }

        val currentBranch = branchResult.output.trim()
        println("${AnsiColors.BLUE}Current branch: ${AnsiColors.CYAN}$currentBranch${AnsiColors.RESET}")
        println()

        // ステータスを表示
        val statusResult = processExecutor.executeWithOutput(arrayOf("git", "status", "--short"))
        if (statusResult.output.isNotEmpty()) {
            println("${AnsiColors.YELLOW}Uncommitted changes:${AnsiColors.RESET}")
            println(statusResult.output)
            println()
        }

        print("${AnsiColors.GREEN}Do you want to push to origin/$currentBranch? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Push cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Pushing to origin/$currentBranch...${AnsiColors.RESET}")

        val pushResult = processExecutor.executeWithOutput(arrayOf("git", "push", "origin", currentBranch))

        if (pushResult.exitCode == 0) {
            println("${AnsiColors.GREEN}✓ Successfully pushed to origin/$currentBranch${AnsiColors.RESET}")
            if (pushResult.output.isNotEmpty()) {
                println(pushResult.output)
            }
        } else {
            println("${AnsiColors.RED}Error pushing to repository:${AnsiColors.RESET}")
            println(pushResult.error)
        }
    }
}