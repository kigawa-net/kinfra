package net.kigawa.kinfra

import net.kigawa.kinfra.di.DependencyContainer

fun main(args: Array<String>) {
    try {
        val container = DependencyContainer()
        val terraformRunner = container.terraformRunner
        terraformRunner.run(args)
    } catch (e: Exception) {
        System.err.println("Fatal error during initialization: ${e.message}")
        e.printStackTrace()
        kotlin.system.exitProcess(1)
    }
}
