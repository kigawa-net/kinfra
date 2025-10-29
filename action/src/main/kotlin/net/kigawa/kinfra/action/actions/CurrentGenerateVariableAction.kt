package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.config.ConfigRepository
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

        // Determine output directory: CLI flag > kinfra.yaml > current directory
        val outputDir = options["output-dir"] ?: run {
            val currentDir = System.getProperty("user.dir")
            val kinfraConfigPath = Paths.get(currentDir, "kinfra.yaml")
            val kinfraParentConfigPath = Paths.get(currentDir, "kinfra-parent.yaml")

            val configOutputDir = if (configRepository.kinfraConfigExists(kinfraConfigPath.toString())) {
                val kinfraConfig = configRepository.loadKinfraConfig(kinfraConfigPath)
                kinfraConfig?.rootProject?.terraform?.generateOutputDir
            } else if (configRepository.kinfraParentConfigExists(kinfraParentConfigPath.toString())) {
                val kinfraParentConfig = configRepository.loadKinfraParentConfig(kinfraParentConfigPath.toString())
                kinfraParentConfig?.terraform?.generateOutputDir
            } else {
                null
            }
            
            configOutputDir ?: currentDir
        }

        val withOutputs = options.containsKey("with-outputs")
        val outputDirFile = File(outputDir)
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }
        val variablesFile = File(outputDirFile, "variables.tf")
        val outputsFile = File(outputDirFile, "outputs.tf")

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

        // Always overwrite the file
        variablesFile.writeText(content)

        if (variablesToGenerate.size == 1) {
            println("${AnsiColors.GREEN}✓ Generated variable '${variablesToGenerate[0]}' in ${variablesFile.absolutePath}${AnsiColors.RESET}")
        } else {
            println("${AnsiColors.GREEN}✓ Generated ${variablesToGenerate.size} variables in ${variablesFile.absolutePath}${AnsiColors.RESET}")
        }

        // Generate outputs.tf if --with-outputs option is specified
        if (withOutputs) {
            val outputsToGenerate = if (remainingArgs.isEmpty()) {
                // Generate all outputs from kinfra.yaml or kinfra-parent.yaml
                val currentDir = System.getProperty("user.dir")
                val kinfraConfigPath = Paths.get(currentDir, "kinfra.yaml")
                val kinfraParentConfigPath = Paths.get(currentDir, "kinfra-parent.yaml")

                val outputMappings = if (configRepository.kinfraConfigExists(kinfraConfigPath.toString())) {
                    val kinfraConfig = configRepository.loadKinfraConfig(kinfraConfigPath)
                    kinfraConfig?.rootProject?.terraform?.outputMappings ?: emptyList()
                } else if (configRepository.kinfraParentConfigExists(kinfraParentConfigPath.toString())) {
                    val kinfraParentConfig = configRepository.loadKinfraParentConfig(kinfraParentConfigPath.toString())
                    kinfraParentConfig?.terraform?.outputMappings ?: emptyList()
                } else {
                    emptyList()
                }

                if (outputMappings.isEmpty()) {
                    println("${AnsiColors.YELLOW}Warning: No output mappings found in kinfra.yaml or kinfra-parent.yaml${AnsiColors.RESET}")
                    return 0
                }
                outputMappings.map { it.terraformOutput }
            } else if (remainingArgs.size == 1) {
                // Generate output with the same name as the variable
                listOf(remainingArgs[0])
            } else {
                emptyList()
            }

            if (outputsToGenerate.isNotEmpty()) {
                val outputsContent = outputsToGenerate.joinToString("\n\n") { outputName ->
                    """
                    |output "$outputName" {
                    |  description = "Generated output: $outputName"
                    |  value       = var.$outputName
                    |  sensitive   = true
                    |}
                    """.trimMargin()
                } + "\n"

                // Always overwrite the file
                outputsFile.writeText(outputsContent)

                if (outputsToGenerate.size == 1) {
                    println("${AnsiColors.GREEN}✓ Generated output '${outputsToGenerate[0]}' in ${outputsFile.absolutePath}${AnsiColors.RESET}")
                } else {
                    println("${AnsiColors.GREEN}✓ Generated ${outputsToGenerate.size} outputs in ${outputsFile.absolutePath}${AnsiColors.RESET}")
                }
            }
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
                "--with-outputs" -> {
                    options["with-outputs"] = "true"
                    i++
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
        println("  --output-dir, -o <dir>    Output directory for variables.tf")
        println("                            Priority: CLI flag > kinfra.yaml terraform.generateOutputDir > current directory")
        println("  --with-outputs            Also generate outputs.tf with corresponding outputs")
        println()
        println("${AnsiColors.BLUE}Configuration (kinfra.yaml):${AnsiColors.RESET}")
        println("  project:")
        println("    terraform:")
        println("      generateOutputDir: /path/to/output  # Default output directory for generated files")
        println()
        println("${AnsiColors.BLUE}Examples:${AnsiColors.RESET}")
        println("  kinfra current generate variable                               # Generate all variables")
        println("  kinfra current generate variable my_var                        # Generate specific variable")
        println("  kinfra current generate variable --with-outputs                # Generate all variables and outputs")
        println("  kinfra current generate variable --with-outputs my_var         # Generate specific variable and output")
        println("  kinfra current generate variable --output-dir /tmp             # Generate to /tmp")
        println("  kinfra current generate variable -o /path/to/dir my_var        # Generate specific variable to dir")
        println("  kinfra current generate variable --with-outputs -o /tmp        # Generate variables and outputs to /tmp")
    }
}