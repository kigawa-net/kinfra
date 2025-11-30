package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.config.LoggerConfig
import net.kigawa.kodel.api.log.config.LoggerConfigNode
import net.kigawa.kodel.api.log.config.formatter.JvmLoggerFormatter
import net.kigawa.kodel.api.log.config.root.RootLoggerConfig
import net.kigawa.kodel.api.log.config.root.RootLoggerConfigureDsl
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
    }

    fun get(clazz: KClass<*>): Logger = get(clazz.qualifiedName!!)
    fun get(name: String): Logger {
        return Logger.getLogger(name)
    }

    fun configure(block: RootLoggerConfigureDsl.() -> Unit) {
        RootLoggerConfigureDsl().apply(block).let {
            configureRoot(it.rootLoggerConfig)
            loggerConfigNode.configure(it)
        }
    }
    private fun configureRoot(rootLoggerConfig: RootLoggerConfig) = Logger.getLogger("").apply {
        val handlerConfig = rootLoggerConfig.rootConsoleHandlerConfig
        handlers.filterIsInstance<ConsoleHandler>().forEach {
            it.level = handlerConfig.level?.primary
            it.formatter = JvmLoggerFormatter(handlerConfig.formatter)
        }
    }
}