package net.kigawa.kinfra.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.action.TerraformService
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.infrastructure.logging.Logger
import net.kigawa.kinfra.infrastructure.process.ProcessExecutor
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.isSuccess
import net.kigawa.kinfra.model.util.message

class HelloAction(
    private val processExecutor: ProcessExecutor,
    private val terraformService: TerraformService,
    private val logger: Logger,
    private val gitHelper: GitHelper
) : Action {

    override fun execute(args: Array<String>): Int {
        // Pull latest changes from git repository
        if (!gitHelper.pullRepository()) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository, continuing anyway...")
        }

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
            MenuItem("Check Git status") { gitStatus() },
            MenuItem("Push to Git repository") { gitPush() },
            MenuItem("Run Terraform init") { terraformInit() },
            MenuItem("Run Terraform plan") { terraformPlan() },
            MenuItem("Run Terraform init + plan") { terraformInitAndPlan() },
            MenuItem("Run Terraform apply") { terraformApply() }
        )
    }

    override fun getDescription(): String {
        return "Interactive management tool for Terraform directories and Git operations"
    }

    private fun showMainMenu(menuItems: List<MenuItem>) {
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}=== Main Menu ===${AnsiColors.RESET}")
        menuItems.forEachIndexed { index, item ->
            println("  ${AnsiColors.CYAN}${index + 1}.${AnsiColors.RESET} ${item.label}")
        }
        println("  ${AnsiColors.CYAN}0.${AnsiColors.RESET} Exit")
        println()
    }

    private fun terraformInit() {
        logger.info("Running terraform init")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Init${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Init cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Initializing Terraform...${AnsiColors.RESET}")

        val result = terraformService.init(quiet = false)

        if (result.isSuccess()) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform init completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform init failed${AnsiColors.RESET}")
            result.message()?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformPlan() {
        logger.info("Running terraform plan")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Plan${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Plan cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Planning Terraform changes...${AnsiColors.RESET}")

        val result = terraformService.plan(quiet = false)

        if (result.isSuccess()) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform plan completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform plan failed${AnsiColors.RESET}")
            result.message()?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformInitAndPlan() {
        logger.info("Running terraform init + plan")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Init + Plan${AnsiColors.RESET}")
        println()

        print("${AnsiColors.GREEN}Do you want to continue? (yes/no):${AnsiColors.RESET} ")
        val confirmation = readLine()?.trim()?.lowercase() ?: ""

        if (confirmation != "yes" && confirmation != "y") {
            println("${AnsiColors.YELLOW}Init + Plan cancelled.${AnsiColors.RESET}")
            return
        }

        println()
        println("${AnsiColors.BLUE}Step 1/2: Initializing Terraform...${AnsiColors.RESET}")

        val initResult = terraformService.init(quiet = false)

        if (!initResult.isSuccess()) {
            println()
            println("${AnsiColors.RED}✗ Terraform init failed${AnsiColors.RESET}")
            initResult.message()?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
            return
        }

        println()
        println("${AnsiColors.GREEN}✓ Terraform init completed successfully${AnsiColors.RESET}")
        println()
        println("${AnsiColors.BLUE}Step 2/2: Planning Terraform changes...${AnsiColors.RESET}")

        val planResult = terraformService.plan(quiet = false)

        if (planResult.isSuccess()) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform init + plan completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform plan failed${AnsiColors.RESET}")
            planResult.message()?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun terraformApply() {
        logger.info("Running terraform apply")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Running Terraform Apply${AnsiColors.RESET}")
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

        val result = terraformService.apply(quiet = false)

        if (result.isSuccess()) {
            println()
            println("${AnsiColors.GREEN}✓ Terraform apply completed successfully${AnsiColors.RESET}")
        } else {
            println()
            println("${AnsiColors.RED}✗ Terraform apply failed${AnsiColors.RESET}")
            result.message()?.let { println("${AnsiColors.RED}Error: $it${AnsiColors.RESET}") }
        }
    }

    private fun gitStatus() {
        logger.info("Checking git status")
        println("${AnsiColors.BLUE}${AnsiColors.BOLD}Git Status${AnsiColors.RESET}")
        println()

        val repoDir = java.io.File(System.getProperty("user.dir"))

        val result = processExecutor.executeWithOutput(
            arrayOf("git", "status"),
            workingDir = repoDir
        )

        if (result.exitCode == 0) {
            println(result.output)
        } else {
            println("${AnsiColors.RED}Error checking git status:${AnsiColors.RESET}")
            println(result.error)
        }
    }

    private fun gitPush() {
        logger.info("Pushing to git repository")

        val repoDir = java.io.File(System.getProperty("user.dir"))

        // まず、現在のブランチを確認（rev-parseの方が互換性が高い）
        val branchResult = processExecutor.executeWithOutput(
            arrayOf("git", "rev-parse", "--abbrev-ref", "HEAD"),
            workingDir = repoDir
        )
        if (branchResult.exitCode != 0) {
            println("${AnsiColors.RED}Error: Could not determine current branch${AnsiColors.RESET}")
            println("${AnsiColors.RED}Details: ${branchResult.error}${AnsiColors.RESET}")
            logger.error("git rev-parse failed: ${branchResult.error}")
            return
        }

        val currentBranch = branchResult.output.trim()
        if (currentBranch.isEmpty()) {
            println("${AnsiColors.RED}Error: Branch name is empty${AnsiColors.RESET}")
            logger.error("git rev-parse returned empty output")
            return
        }
        println("${AnsiColors.BLUE}Current branch: ${AnsiColors.CYAN}$currentBranch${AnsiColors.RESET}")
        println()

        // ステータスを表示
        val statusResult = processExecutor.executeWithOutput(
            arrayOf("git", "status", "--short"),
            workingDir = repoDir
        )
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

        val pushResult = processExecutor.executeWithOutput(
            arrayOf("git", "push", "origin", currentBranch),
            workingDir = repoDir
        )

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
