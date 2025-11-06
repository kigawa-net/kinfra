package net.kigawa.kinfra

import kotlinx.coroutines.runBlocking
import net.kigawa.kinfra.cli.CliDeps
import net.kigawa.kinfra.cli.CliScope
import net.kigawa.kodel.api.dep.DepContext

fun main(args: Array<String>) {
    val context = DepContext(CliScope.create())
    try {
        runBlocking {
            CliDeps(context).main(args)
        }
    } catch (e: Exception) {
        System.err.println("Fatal error during initialization: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    } finally {
        runBlocking {
            context.close()
        }
    }
}
