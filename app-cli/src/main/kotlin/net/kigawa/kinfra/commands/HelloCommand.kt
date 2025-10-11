package net.kigawa.kinfra.commands

import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.infrastructure.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.infrastructure.terraform.TerraformVarsManager
import net.kigawa.kinfra.model.Command
import net.kigawa.kinfra.model.Environment
import net.kigawa.kinfra.model.HostsConfig
import net.kigawa.kinfra.util.AnsiColors

class HelloCommand(
    private val configRepository: ConfigRepository,
    private val terraformVarsManager: TerraformVarsManager,
    private val processExecutor: ProcessExecutor,
    private val terraformService: TerraformService,
    private val logger: Logger
) : Command {
    private var currentEnvironment: Environment = Environment.PROD

    override fun execute(args: Array<String>): Int {
        println("${AnsiColors.CYAN}${AnsiColors.BOLD}Welcome to kinfra interactive manager!${AnsiColors.RESET}")
        println()

        while (true) {
            val menuItems = buildMenuItems()
            showMainMenu(menuItems)
            print("${AnsiColors.GREEN}Select an option (1-${menuItems.size}):${AnsiColors.RESET} ")
            val choice = readLine()?.trim() ?: ""

            println()
            when (choice) {
                "0", "q", "quit", "exit" -> {
                    println("${AnsiColors.CYAN}Goodbye!${AnsiColors.RESET}")
                    return 0
                }
                else -> {
                    val index = choice.toIntOrNull()?.minus(1)
                    if (index != null && index in menuItems.indices) {
                        menuItems[index].action()
                    } else {
                        println("${AnsiColors.RED}Invalid option. Please try again.${AnsiColors.RESET}")
                    }
                }
            }
            println()
        }
    }

    private data class MenuItem(
        val label: String,
        val action: () -> Unit
    )

    private fun buildMenuItems(): List<MenuItem> {
        return listOf(
            MenuItem("List Terraform directories") { listHosts() },
            MenuItem("Enable Terraform directory") { enableHost() },
            MenuItem("Disable Terraform directory") { disableHost() },
            MenuItem("Check Git status") { gitStatus() },
            MenuItem("Push to Git repository") { gitPush() },
            MenuItem("Select environment (current: ${currentEnvironment.name})") { selectEnvironment() },
            MenuItem("Run Terraform init") { terraformInit() },
            MenuItem("Run Terraform plan") { terraformPlan() },
            MenuItem("Run Terraform init + plan") { terraformInitAndPlan() },
            MenuItem("Run Terraform apply") { terraformApply() }
        )
    }

    override fun getDescription(): String {
        return "Interactive management tool for Terraform directories and Git operations"
    }

    override fun requiresEnvironment(): Boolean {
        return false
    }

    private fun showMainMenu(menuItems: List<MenuItem>) {
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}=== Main Menu ===${AnsiColors.RESET}")
        menuItems.forEachIndexed { index, item ->
            println("  ${AnsiColors.CYAN}${index + 1}.${AnsiColors.RESET} ${item.label}")
        }
        println("  ${AnsiColors.CYAN}0.${AnsiColors.RESET} Exit")
        println()
    }

    private fun selectEnvironment() {
        logger.info("Selecting environment")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Select Environment${AnsiColors.RESET}")
        println("${AnsiColors.YELLOW}Note: Currently only 'prod' is supported${AnsiColors.RESET}")
        println()
        println("  1. prod ${if (currentEnvironment.name == "prod") "${AnsiColors.GREEN}(current)${AnsiColors.RESET}" else ""}")
        println()

        print("${AnsiColors.GREEN}Enter environment name or number:${AnsiColors.RESET} ")
        val input = readLine()?.trim()?.lowercase() ?: ""

        val envName = when (input) {
            "1" -> "prod"
            else -> input
        }

        if (Environment.isValid(envName)) {
            currentEnvironment = Environment.fromString(envName) ?: Environment.PROD
            println("${AnsiColors.GREEN}✓ Environment set to: ${currentEnvironment.name}${AnsiColors.RESET}")
        } else {
            println("${AnsiColors.RED}Error: Invalid environment '$envName'${AnsiColors.RESET}")
        }
    }

    private fun terraformInit() {
        logger.info("Running terraform init for environment: ${currentEnvironment.name}")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Init${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Environment: ${currentEnvironment.name}${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Init cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")

        val result = terraformService.init(currentEnvironment, quiet = false)

        if (result.isSuccess) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform init completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform init failed${AnsiColors.RESET}")
            result.message?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformPlan() {
        logger.info("Running terraform plan for environment: ${currentEnvironment.name}")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Plan${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Environment: ${currentEnvironment.name}${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Plan cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Planning Terraform changes...${AnsiColors.RESET}")

        val result = terraformService.plan(currentEnvironment, quiet = false)

        if (result.isSuccess) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform plan completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform plan failed${AnsiColors.RESET}")
            result.message?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformInitAndPlan() {
        logger.info("Running terraform init + plan for environment: ${currentEnvironment.name}")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Init + Plan${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Environment: ${currentEnvironment.name}${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Init + Plan cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Step 1/2: Initializing Terraform...${AnsiColors.RESET}")

        val initResult = terraformService.init(currentEnvironment, quiet = false)

        if (!initResult.isSuccess) {
            println()
            println("${AnsiColors.RED}✗ Terraform init failed${AnsiColors.RESET}")
            initResult.message?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
            return
        }

        println()
        println("${AnsiColors.GREEN}✓ Terraform init completed successfully${AnsiColors.RESET}")
        println()
        println("${AnsiColors.BLUE}Step 2/2: Planning Terraform changes...${AnsiColors.RESET}")

        val planResult = terraformService.plan(currentEnvironment, quiet = false)

        if (planResult.isSuccess) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform init + plan completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform plan failed${AnsiColors.RESET}")
            planResult.message?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformApply() {
        logger.info("Running terraform apply for environment: ${currentEnvironment.name}")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Apply${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Environment: ${currentEnvironment.name}${AnsiColors.RESET}")
        println("${AnsiColors.YELLOW}Warning: This will make changes to your infrastructure!${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Apply cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Applying Terraform changes...${AnsiColors.RESET}")

        val result = terraformService.apply(currentEnvironment, quiet = false)

        if (result.isSuccess) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform apply completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform apply failed${AnsiColors.RESET}")
            result.message?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
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