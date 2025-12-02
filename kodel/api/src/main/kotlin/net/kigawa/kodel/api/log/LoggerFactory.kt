package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.config.LoggerNode
import net.kigawa.kodel.api.log.config.root.RootLoggerConfigureDsl
import java.util.logging.Logger
import kotlin.reflect.KClass

object LoggerFactory {
    val loggerNode = LoggerNode(
        null, ""
    )

    init {
        System.getProperty("jdk.logger.packages")
            ?.let { "$it," }
            .let { it ?: "" }
            .let {
                System.setProperty("jdk.logger.packages", it + "net.kigawa.kodel.api.log.traceignore")
            }
    }

    fun get(clazz: KClass<*>): Kogger = get(clazz.qualifiedName!!)
    internal fun getInternal(name: String): Kogger = Logger.getLogger(name)
    fun get(name: String): Kogger {
        return getInternal(name)
    }

    fun configure(block: RootLoggerConfigureDsl.() -> Unit) {
        RootLoggerConfigureDsl().apply(block).let {
            configureRoot()
            loggerNode.configureDsl(it)
        }
    }

    private fun configureRoot() = get("").apply {
        handlers.forEach {
            removeHandler(it)
        }
    }
}