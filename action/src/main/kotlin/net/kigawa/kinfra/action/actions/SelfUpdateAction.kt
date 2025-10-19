package net.kigawa.kinfra.action.actions

import net.kigawa.kinfra.action.execution.UpdateProcessor
import net.kigawa.kinfra.action.logging.Logger
import net.kigawa.kinfra.action.update.AutoUpdater
import net.kigawa.kinfra.action.update.VersionChecker
import net.kigawa.kinfra.action.GitHelper
import net.kigawa.kinfra.model.Action
import net.kigawa.kinfra.model.LoginRepo

class SelfUpdateAction(
    private val versionChecker: VersionChecker,
    private val autoUpdater: AutoUpdater,
    private val gitHelper: GitHelper,
    val loginRepo: LoginRepo,
    private val logger: Logger
) : Action {
    
    private val updateProcessor = UpdateProcessor(
        versionChecker,
        autoUpdater,
        gitHelper,
        loginRepo,
        logger
    )

    override fun execute(args: List<String>): Int {
        return updateProcessor.performUpdate(args)
    }
    
    override fun getDescription(): String {
        return "Update kinfra to the latest version"
    }
}