package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.GitHelper
import net.kigawa.kinfra.model.LoginRepo
import net.kigawa.kinfra.model.execution.UpdateProcessor
import net.kigawa.kinfra.model.update.AutoUpdater
import net.kigawa.kinfra.model.update.VersionChecker

class SelfUpdateAction(
    versionChecker: VersionChecker,
    autoUpdater: AutoUpdater,
    gitHelper: GitHelper,
    val loginRepo: LoginRepo,
) : Action {
    private val updateProcessor =
        UpdateProcessor(
            versionChecker,
            autoUpdater,
            gitHelper,
            loginRepo,
        )

    override fun execute(args: List<String>): Int {
        return updateProcessor.performUpdate(args)
    }

    override fun getDescription(): String {
        return "Update kinfra to the latest version"
    }
}
