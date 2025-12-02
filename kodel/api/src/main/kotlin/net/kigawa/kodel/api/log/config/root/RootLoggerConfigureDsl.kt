package net.kigawa.kodel.api.log.config.root

import net.kigawa.kodel.api.log.config.LoggerConfigureDsl
import kotlin.reflect.KClass

class RootLoggerConfigureDsl: LoggerConfigureDsl() {

    fun classConfig(clazz: KClass<*>, block: LoggerConfigureDsl.() -> Unit) {
        child(clazz.qualifiedName!!, block)
    }
}