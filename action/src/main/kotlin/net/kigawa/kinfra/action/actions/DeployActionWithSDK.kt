package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.service.TerraformService
import net.kigawa.kinfra.action.bitwarden.BitwardenSecretManagerRepository
import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.action.config.EnvFileLoader
import net.kigawa.kinfra.action.execution.SubProjectExecutor
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.R2BackendConfig
import net.kigawa.kinfra.model.util.AnsiColors
import net.kigawa.kinfra.model.util.exitCode
import net.kigawa.kinfra.model.util.isFailure
import net.kigawa.kinfra.model.util.isSuccess
import net.kigawa.kinfra.model.util.message
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.BitwardenSecret
import java.io.File

/**
 * Bitwarden Secret Manager SDK を使用したデプロイコマンド
 */
class DeployActionWithSDK(
    private val terraformService: TerraformService,
    private val secretManagerRepository: BitwardenSecretManagerRepository,
    private val configRepository: ConfigRepository,
    private val loginRepo: LoginRepo,
    private val logger: Logger,
    private val envFileLoader: EnvFileLoader
): Action {

    private val subProjectExecutor = SubProjectExecutor(configRepository, loginRepo)

    override fun execute(args: List<String>): Int {
        logger.info("DeployActionWithSDK started with args: ${args.joinToString(" ")}")

        val additionalArgs = args.filter { it != "--auto-selected" }

        println("${AnsiColors.BLUE}Starting full deployment pipeline${AnsiColors.RESET}")
        println("${AnsiColors.BLUE}Current working directory: ${System.getProperty("user.dir")}${AnsiColors.RESET}")
        println()

        // Execute parent project first
        println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}Executing parent project${AnsiColors.RESET}")
        println("${AnsiColors.CYAN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${AnsiColors.RESET}")
        println()

        // Step 1: Initialize
        logger.info("Step 1: Initializing Terraform")
        println("${AnsiColors.BLUE}Step 1/3: Initializing Terraform${AnsiColors.RESET}")
        val initResult = terraformService.init(emptyList())
        if (initResult.isFailure()) {
            logger.error("Terraform init failed with exit code: ${initResult.exitCode()}")
            println("${AnsiColors.RED}Terraform init failed: ${initResult.message()} (exit code: ${initResult.exitCode()})${AnsiColors.RESET}")
            println("${AnsiColors.RED}Parent project deployment failed${AnsiColors.RESET}")
            return initResult.exitCode()
        }
        logger.info("Terraform init completed successfully")

        println()

        // Step 2: Plan
        logger.info("Step 2: Creating execution plan")
        println("${AnsiColors.BLUE}Step 2/3: Creating execution plan${AnsiColors.RESET}")
        val planResult = terraformService.plan(additionalArgs, planFile = "tfplan")
        if (planResult.isFailure()) {
            logger.error("Terraform plan failed with exit code: ${planResult.exitCode()}")
            println("${AnsiColors.RED}Terraform plan failed: ${planResult.message()} (exit code: ${planResult.exitCode()})${AnsiColors.RESET}")
            println("${AnsiColors.RED}Parent project deployment failed${AnsiColors.RESET}")
            return planResult.exitCode()
        }
        logger.info("Terraform plan completed successfully")

        println()

        // Step 3: Apply
        logger.info("Step 3: Applying changes")
        println("${AnsiColors.BLUE}Step 3/3: Applying changes${AnsiColors.RESET}")
        val applyArgsWithAutoApprove = if (additionalArgs.contains("-auto-approve")) {
            additionalArgs
        } else {
            additionalArgs + "-auto-approve"
        }
        val applyResult = terraformService.apply(planFile = "tfplan", additionalArgs = applyArgsWithAutoApprove)

        if (applyResult.isFailure()) {
            logger.error("Terraform apply failed with exit code: ${applyResult.exitCode()}")
            println("${AnsiColors.RED}Terraform apply failed: ${applyResult.message()} (exit code: ${applyResult.exitCode()})${AnsiColors.RESET}")
            println("${AnsiColors.RED}Parent project deployment failed${AnsiColors.RESET}")
            return applyResult.exitCode()
        }

        logger.info("Parent project deployment completed successfully")
        println()
        println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Parent project completed successfully")

        // Execute sub-projects
        val subProjects = subProjectExecutor.getSubProjects()
        if (subProjects.isNotEmpty()) {
            println()
            println("${AnsiColors.BLUE}Found ${subProjects.size} sub-project(s)${AnsiColors.RESET}")

            val subResult = subProjectExecutor.executeInSubProjects(subProjects) { subProject, subProjectDir ->
                executeSubProjectDeployment(additionalArgs, subProjectDir)
            }

            if (subResult != 0) {
                println("${AnsiColors.RED}Sub-project deployment failed${AnsiColors.RESET}")
                return subResult
            }
        }

        // Handle post-deployment actions
        logger.info("All deployments completed successfully")
        println()
        println("${AnsiColors.GREEN}✅ Deployment completed successfully!${AnsiColors.RESET}")

        // Auto git push after successful deployment
        println()
        println("${AnsiColors.BLUE}Pushing to remote repository...${AnsiColors.RESET}")
        val pushResult = gitPush()
        if (pushResult) {
            logger.info("Successfully pushed to remote repository")
            println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Successfully pushed to remote repository")
        } else {
            logger.warn("Failed to push to remote repository")
            println("${AnsiColors.YELLOW}⚠${AnsiColors.RESET} Failed to push to remote repository (non-fatal)")
        }

        return 0
    }

    private fun executeSubProjectDeployment(additionalArgs: List<String>, subProjectDir: File): Int {
        logger.info("Deploying sub-project in directory: ${subProjectDir.absolutePath}")

        // Step 1: Initialize
        logger.info("Step 1: Initializing Terraform for sub-project")
        println("${AnsiColors.BLUE}Step 1/3: Initializing Terraform${AnsiColors.RESET}")
        val initResult = terraformService.init(emptyList())
        if (initResult.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (initResult.message()?.contains("Terraform configuration not found") == true) {
                logger.info("Terraform configuration not found for sub-project, skipping")
                println("${AnsiColors.YELLOW}⚠ Terraform configuration not found, skipping${AnsiColors.RESET}")
                return 0
            }
            logger.error("Terraform init failed for sub-project with exit code: ${initResult.exitCode()}")
            return initResult.exitCode()
        }
        logger.info("Terraform init completed successfully for sub-project")

        println()

        // Step 2: Plan
        logger.info("Step 2: Creating execution plan for sub-project")
        println("${AnsiColors.BLUE}Step 2/3: Creating execution plan${AnsiColors.RESET}")
        val planResult = terraformService.plan(additionalArgs)
        if (planResult.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (planResult.message()?.contains("Terraform configuration not found") == true) {
                logger.info("Terraform configuration not found for sub-project, skipping")
                println("${AnsiColors.YELLOW}⚠ Terraform configuration not found, skipping${AnsiColors.RESET}")
                return 0
            }
            logger.error("Terraform plan failed for sub-project with exit code: ${planResult.exitCode()}")
            return planResult.exitCode()
        }
        logger.info("Terraform plan completed successfully for sub-project")

        println()

        // Step 3: Apply
        logger.info("Step 3: Applying changes for sub-project")
        println("${AnsiColors.BLUE}Step 3/3: Applying changes${AnsiColors.RESET}")
        val applyArgsWithAutoApprove = if (additionalArgs.contains("-auto-approve")) {
            additionalArgs
        } else {
            additionalArgs + "-auto-approve"
        }
        val applyResult = terraformService.apply(additionalArgs = applyArgsWithAutoApprove)

        if (applyResult.isFailure()) {
            // Terraform設定がない場合はスキップとして成功扱い
            if (applyResult.message()?.contains("Terraform configuration not found") == true) {
                logger.info("Terraform configuration not found for sub-project, skipping")
                println("${AnsiColors.YELLOW}⚠ Terraform configuration not found, skipping${AnsiColors.RESET}")
                return 0
            }
            logger.error("Terraform apply failed for sub-project with exit code: ${applyResult.exitCode()}")
            return applyResult.exitCode()
        }

        logger.info("Sub-project deployment completed successfully")
        return 0
    }

    override fun getDescription(): String {
        return "Full deployment pipeline using Secret Manager SDK (init → plan → apply)"
    }



    private fun gitPush(): Boolean {
        return try {
            logger.debug("Executing git push")
            val process = ProcessBuilder("git", "push")
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val exitCode = process.waitFor()
            if (exitCode != 0) {
                val error = process.errorStream.bufferedReader().readText()
                logger.warn("Git push failed with exit code $exitCode: $error")
                println("${AnsiColors.YELLOW}Git push failed: $error${AnsiColors.RESET}")
                false
            } else {
                logger.debug("Git push completed successfully")
                true
            }
        } catch (e: Exception) {
            logger.error("Git push error", e)
            println("${AnsiColors.YELLOW}Git push error: ${e.message}${AnsiColors.RESET}")
            false
        }
    }
}
