package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.config.ConfigRepository
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.util.AnsiColors
import java.io.File
import java.nio.file.Paths

class CurrentGenerateVariableAction(private val configRepository: ConfigRepository) : Action {
    override fun execute(args: List<String>): Int {
        if (args.isEmpty() || args[0] != "variable") {
            println("${AnsiColors.RED}Error: Invalid arguments${AnsiColors.RESET}")
            println("Usage: kinfra current generate variable [options] [variable_name]")
            return 1
        }

        // Parse options
        val (options, remainingArgs) = parseOptions(args.drop(1))
        val outputDir = options["output-dir"] ?: System.getProperty("user.dir")
        val outputDirFile = File(outputDir)
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }
        val variablesFile = File(outputDirFile, "variables.tf")

        val variablesToGenerate = if (remainingArgs.isEmpty()) {
            // Generate all variables from kinfra.yaml or kinfra-parent.yaml
            val currentDir = System.getProperty("user.dir")
            val kinfraConfigPath = Paths.get(currentDir, "kinfra.yaml")
            val kinfraParentConfigPath = Paths.get(currentDir, "kinfra-parent.yaml")

            val variableMappings = if (configRepository.kinfraConfigExists(kinfraConfigPath.toString())) {
                val kinfraConfig = configRepository.loadKinfraConfig(kinfraConfigPath)
                kinfraConfig?.rootProject?.terraform?.variableMappings ?: emptyList()
            } else if (configRepository.kinfraParentConfigExists(kinfraParentConfigPath.toString())) {
                val kinfraParentConfig = configRepository.loadKinfraParentConfig(kinfraParentConfigPath.toString())
                kinfraParentConfig?.terraform?.variableMappings ?: emptyList()
            } else {
                emptyList()
            }

            if (variableMappings.isEmpty()) {
                println("${AnsiColors.YELLOW}Warning: No variable mappings found in kinfra.yaml or kinfra-parent.yaml${AnsiColors.RESET}")
                return 0
            }
            variableMappings.map { it.terraformVariable }
        } else if (remainingArgs.size == 1) {
            // Generate specific variable
            listOf(remainingArgs[0])
        } else {
            println("${AnsiColors.RED}Error: Too many arguments${AnsiColors.RESET}")
            println("Usage: kinfra current generate variable [options] [variable_name]")
            return 1
        }

        val content = variablesToGenerate.joinToString("\n\n") { variableName ->
            """
            |variable "$variableName" {
            |  description = "Generated variable: $variableName"
            |  type        = string
            |  default     = ""
            |}
            """.trimMargin()
        } + "\n"

        if (variablesFile.exists()) {
            // Append to existing file
            variablesFile.appendText("\n$content")
        } else {
            // Create new file
            variablesFile.writeText(content)
        }

        if (variablesToGenerate.size == 1) {
            println("${AnsiColors.GREEN}✓ Generated variable '${variablesToGenerate[0]}' in ${variablesFile.absolutePath}${AnsiColors.RESET}")
        } else {
            println("${AnsiColors.GREEN}✓ Generated ${variablesToGenerate.size} variables in ${variablesFile.absolutePath}${AnsiColors.RESET}")
        }
        return 0
    }

    private fun parseOptions(args: List<String>): Pair<Map<String, String>, List<String>> {
        val options = mutableMapOf<String, String>()
        val remainingArgs = mutableListOf<String>()
        var i = 0
        while (i < args.size) {
            when (args[i]) {
                "--output-dir", "-o" -> {
                    if (i + 1 < args.size) {
                        options["output-dir"] = args[i + 1]
                        i += 2
                    } else {
                        println("${AnsiColors.RED}Error: --output-dir requires a value${AnsiColors.RESET}")
                        return Pair(emptyMap(), emptyList())
                    }
                }
                else -> {
                    remainingArgs.add(args[i])
                    i++
                }
            }
        }
        return Pair(options, remainingArgs)
    }

    override fun getDescription(): String {
        return "Generate a Terraform variable in the current directory"
    }

    override fun showHelp() {
        super.showHelp()
        println("${AnsiColors.BLUE}Usage:${AnsiColors.RESET}")
        println("  kinfra current generate variable [options] [variable_name]")
        println("  If variable_name is not specified, generates all variables from kinfra.yaml")
        println()
        println("${AnsiColors.BLUE}Options:${AnsiColors.RESET}")
        println("  --output-dir, -o <dir>    Output directory for variables.tf (default: current directory)")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra current generate variable                           # Generate all variables")
        println("  kinfra current generate variable my_var                    # Generate specific variable")
        println("  kinfra current generate variable --output-dir /tmp        # Generate to /tmp")
        println("  kinfra current generate variable -o /path/to/dir my_var   # Generate specific variable to dir")
    }
}