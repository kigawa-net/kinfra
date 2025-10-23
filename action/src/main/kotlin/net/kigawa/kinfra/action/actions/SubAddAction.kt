package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.conf.KinfraParentConfigData
import net.kigawa.kinfra.model.sub.SubProject
import net.kigawa.kinfra.model.util.AnsiColors

class SubAddAction(
    private val loginRepo: LoginRepo
) : Action {

    override fun execute(args: List<String>): Int {
        if (args.size < 2) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Sub-project name and path are required")
            println("Usage: kinfra sub add <project-name> <project-path>")
            return 1
        }

        val baseConfig = loginRepo.loadKinfraBaseConfig()
        val subProjectName = args[0].trim()
        val subProjectPath = args[1].trim()
        val subProject: SubProject? = if (baseConfig != null) {
            baseConfig.addSubProject(subProjectName, subProjectPath)
        } else {
            null
        }

        if (baseConfig == null) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Parent configuration not found")
            println("${AnsiColors.BLUE}Creating new parent configuration...${AnsiColors.RESET}")

              // Create default parent config
              val defaultConfigData = KinfraParentConfigData(
                  projectName = "my-infrastructure",
                  description = "Parent project for managing multiple infrastructure components",
                  subProjects = listOf()
              )

              try {
                  val defaultConfig = loginRepo.createKinfraParentConfig(defaultConfigData)
                  val defaultSubProject = defaultConfig.addSubProject(subProjectName, subProjectPath)
                  val updatedData = defaultConfigData.copy(subProjects = listOf(defaultSubProject))
                  defaultConfig.saveData(updatedData)
                  val displayText = "${defaultSubProject.name}:${defaultSubProject.path}"
                  println("${AnsiColors.GREEN}Created:${AnsiColors.RESET} Parent configuration with sub-project '$displayText'")
                  return 0
              } catch (e: Exception) {
                  println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create parent configuration: ${e.message}")
                  return 1
              }
        }

        if (subProject == null) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to create sub-project")
            return 1
        }

        if (baseConfig.subProjects.any { it.name == subProject.name }) {
            println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Sub-project '${subProject.name}' already exists")
            return 0
        }

        // Add the new sub-project
        val currentData = baseConfig.toData()
        val updatedData = currentData.copy(
            subProjects = currentData.subProjects + subProject
        )

        try {
            baseConfig.saveData(updatedData)
            val displayText = "${subProject.name}:${subProject.path}"
            println("${AnsiColors.GREEN}Added:${AnsiColors.RESET} Sub-project '$displayText'")
            println("${AnsiColors.BLUE}Total sub-projects:${AnsiColors.RESET} ${updatedData.subProjects.size}")
        } catch (e: Exception) {
            println("${AnsiColors.RED}Error:${AnsiColors.RESET} Failed to save configuration: ${e.message}")
            return 1
        }

        return 0
    }

    override fun getDescription(): String {
        return "Add a new sub-project to kinfra-parent.yaml (usage: kinfra sub add <name> <path>)"
    }
}