package net.kigawa.kinfra.commands

import net.kigawa.kinfra.infrastructure.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.terraform.TerraformVarsManager
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.HostConfig
import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.util.AnsiColors

class ConfigCommand(
    private val configRepository: ConfigRepository,
    private val logger: Logger,
    private val terraformVarsManager: TerraformVarsManager
) : Command {

    override fun execute(args: Array<String>): Int {
        logger.info("Executing config command with args: ${args.joinToString(" ")}")

        if (args.isEmpty()) {
            printUsage()
            return 1
        }

        return when (args[0]) {
            "list" -> listHosts()
            "enable" -> {
                if (args.size < 2) {
                    println("${AnsiColors.RED}Error:${AnsiColors.RESET} Host name is required")
                    println("Usage: config enable <host-name>")
                    return 1
                }
                enableHost(args[1])
            }
            "disable" -> {
                if (args.size < 2) {
                    println("${AnsiColors.RED}Error:${AnsiColors.RESET} Host name is required")
                    println("Usage: config disable <host-name>")
                    return 1
                }
                disableHost(args[1])
            }
            else -> {
                println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown subcommand: ${args[0]}")
                printUsage()
                1
            }
        }
    }

    override fun getDescription(): String {
        return "Manage host configuration (enable/disable hosts)"
    }


    private fun printUsage() {
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  config list                  - List all hosts and their status")
        println("  config enable <host-name>    - Enable a host")
        println("  config disable <host-name>   - Disable a host")
        println()
        println("${AnsiColors.BLUE}Available hosts:${AnsiColors.RESET}")
        HostsConfig.DEFAULT_HOSTS.keys.forEach { host ->
            println("  - $host")
        }
    }

    private fun listHosts(): Int {
        val config = configRepository.loadHostsConfig()
        val configPath = configRepository.getConfigFilePath()

        println("${AnsiColors.BLUE}Host Configuration${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Config file: $configPath${AnsiColors.RESET}")
        println()

        val hosts = mutableListOf<HostConfig>()
        HostsConfig.DEFAULT_HOSTS.keys.forEach { hostName ->
            val enabled = config.hosts[hostName] ?: HostsConfig.DEFAULT_HOSTS[hostName] ?: false
            val description = HostsConfig.HOST_DESCRIPTIONS[hostName] ?: "No description"
            hosts.add(HostConfig(hostName, enabled, description))
        }

        hosts.forEach { host ->
            val status = if (host.enabled) "${AnsiColors.GREEN}enabled${AnsiColors.RESET}" else "${AnsiColors.YELLOW}disabled${AnsiColors.RESET}"
            println("  ${host.name.padEnd(15)} [$status]  ${host.description}")
        }

        return 0
    }

    private fun enableHost(hostName: String): Int {
        if (!HostsConfig.DEFAULT_HOSTS.containsKey(hostName)) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown host: $hostName")
            println()
            println("${AnsiColors.BLUE}Available hosts:${AnsiColors.RESET}")
            HostsConfig.DEFAULT_HOSTS.keys.forEach { host ->
                println("  - $host")
            }
            return 1
        }

        val config = configRepository.loadHostsConfig()
        val updatedHosts = config.hosts.toMutableMap()
        updatedHosts[hostName] = true

        configRepository.saveHostsConfig(HostsConfig(updatedHosts))
        logger.info("Host $hostName enabled")

        // Terraform変数ファイルを更新
        terraformVarsManager.updateHostsVars()
        val varsPath = terraformVarsManager.getVarsFilePath()
        logger.info("Updated Terraform vars file: $varsPath")

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Host ${AnsiColors.CYAN}$hostName${AnsiColors.RESET} has been ${AnsiColors.GREEN}enabled${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Updated Terraform vars: $varsPath${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Note:${AnsiColors.RESET} Run 'init' and 'apply' to apply changes to Terraform infrastructure")

        return 0
    }

    private fun disableHost(hostName: String): Int {
        if (!HostsConfig.DEFAULT_HOSTS.containsKey(hostName)) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Unknown host: $hostName")
            println()
            println("${AnsiColors.BLUE}Available hosts:${AnsiColors.RESET}")
            HostsConfig.DEFAULT_HOSTS.keys.forEach { host ->
                println("  - $host")
            }
            return 1
        }

        val config = configRepository.loadHostsConfig()
        val updatedHosts = config.hosts.toMutableMap()
        updatedHosts[hostName] = false

        configRepository.saveHostsConfig(HostsConfig(updatedHosts))
        logger.info("Host $hostName disabled")

        // Terraform変数ファイルを更新
        terraformVarsManager.updateHostsVars()
        val varsPath = terraformVarsManager.getVarsFilePath()
        logger.info("Updated Terraform vars file: $varsPath")

        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Host ${AnsiColors.CYAN}$hostName${AnsiColors.RESET} has been ${AnsiColors.YELLOW}disabled${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Updated Terraform vars: $varsPath${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Note:${AnsiColors.RESET} Run 'init' and 'apply' to apply changes to Terraform infrastructure")

        return 0
    }
}