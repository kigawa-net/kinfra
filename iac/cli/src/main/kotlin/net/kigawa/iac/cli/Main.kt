package net.kigawa.iac.cli

import kotlinx.coroutines.runBlocking
import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.LoggerFactory
import net.kigawa.kodel.api.log.handler.StdHandler

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        LoggerFactory.configure {
            level = LogLevel.INFO
            handler(::StdHandler) {
                level = LogLevel.DEBUG
            }
            child("net.kigawa.kinfra"){
                level = LogLevel.DEBUG
            }
        }
        runBlocking {
            IacCliDeps(
                DepContext(IacCliDepsScope.create(), Main::class)
            ).main()
        }
    }
}