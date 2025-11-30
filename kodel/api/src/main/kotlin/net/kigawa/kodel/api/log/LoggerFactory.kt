package net.kigawa.kodel.api.log

import java.util.logging.ConsoleHandler
import java.util.logging.Logger
import kotlin.reflect.KClass

object LoggerFactory {
    val loggerConfigNode = LoggerConfigNode(
        null, "", LoggerConfig(
            level = LogLevel.INFO,
            handlers = mutableListOf()
        )
    )
    val defaultConfig = LoggerConfig(
        level = LogLevel.INFO,
        handlers = mutableListOf()
    )

    init {
        System.getProperty("jdk.logger.packages")
            ?.let { "$it," }
            .let { it ?: "" }
            .let {
                System.setProperty("jdk.logger.packages", it + "net.kigawa.kodel.api.log.traceignore")
            }
        get("")
        val root = Logger.getLogger("")
        root.handlers.forEach {
            it
        }
    }

    fun get(clazz: KClass<*>): Logger = get(clazz.qualifiedName!!)
    fun get(name: String): Logger {
        return Logger.getLogger(name).apply {
            handlers.filterIsInstance<ConsoleHandler>().forEach {
                it.formatter = DefaultLoggerFormatter()
            }
        }
    }

    fun configure(block: LoggerConfigureDsl.() -> Unit) {
        LoggerConfigureDsl().apply(block).let {
            loggerConfigNode.configure(it)
        }
    }
}