package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action

class PushAction(
     private val gitHelper: GitHelper
 ) : Action {
     override fun execute(args: Array<String>): Int {
         // Add all changes
         val addSuccess = gitHelper.addChanges()
         if (!addSuccess) {
             println("${net.kigawa.kinfra.model.util.AnsiColors.RED}Failed to add changes${net.kigawa.kinfra.model.util.AnsiColors.RESET}")
             return 1
         }

         // Commit changes with a default message
         val commitMessage = args.getOrNull(0) ?: "Auto commit by kinfra push"
         val commitSuccess = gitHelper.commitChanges(commitMessage)
         if (!commitSuccess) {
             println("${net.kigawa.kinfra.model.util.AnsiColors.RED}Failed to commit changes${net.kigawa.kinfra.model.util.AnsiColors.RESET}")
             return 1
         }

         // Push to remote
         val pushSuccess = gitHelper.pushToRemote()
         return if (pushSuccess) 0 else 1
     }

     override fun getDescription(): String {
         return "Add, commit, and push changes to remote repository"
     }
 }
