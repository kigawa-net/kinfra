package net.kigawa.iac.cli

import kotlinx.coroutines.runBlocking
import net.kigawa.kodel.api.dep.DepContext

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        runBlocking {
            IacCliDeps(
                DepContext(IacCliDepsScope.create())
            ).main()

        }
    }
}