package net.kigawa.iac.cli

import kotlinx.coroutines.runBlocking
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.log.LoggerFactory

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        LoggerFactory.configure {
        }
        runBlocking {
            IacCliDeps(
                DepContext(IacCliDepsScope.create(), Main::class)
            ).main()
        }
    }
}