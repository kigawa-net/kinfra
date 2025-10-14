package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action

class PushAction(
    private val gitHelper: GitHelper
) : Action {
    override fun execute(args: Array<String>): Int {
        val success = gitHelper.pushToRemote()
        return if (success) 0 else 1
    }

    override fun getDescription(): String {
        return "Push changes to remote repository"
    }
}
