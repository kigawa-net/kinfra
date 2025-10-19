package net.kigawa.kinfra.service

import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.model.util.AnsiColors
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SystemRequirement : KoinComponent {
    private val logger: Logger by inject()

    fun isTerraformInstalled(): Boolean {
        return try {
            val process = ProcessBuilder("terraform", "version")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    fun validateTerraformInstallation(): Boolean {
        if (!isTerraformInstalled()) {
            logger.error("Terraform is not installed")
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Terraform is not installed or not found in PATH.")
            println("${AnsiColors.BLUE}Please install Terraform:${AnsiColors.RESET}")
            println("  Ubuntu/Debian: sudo apt-get install terraform")
            println("  macOS: brew install terraform")
            println("  Or download from: https://www.terraform.io/downloads.html")
            return false
        }
        return true
    }
}