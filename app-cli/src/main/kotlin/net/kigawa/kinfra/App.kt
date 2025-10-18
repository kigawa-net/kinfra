package net.kigawa.kinfra

import net.kigawa.kinfra.di.appModule
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.java.KoinJavaComponent.inject

fun main(args: Array<String>) {
    try {
        startKoin {
            modules(appModule)
        }

        try {
            val terraformRunner by inject<TerraformRunner>(TerraformRunner::class.java)
            terraformRunner.run(args)
        } finally {
            stopKoin()
        }
    } catch (e: Exception) {
        System.err.println("Fatal error during initialization: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}
