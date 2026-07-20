package net.kigawa.kodel.api.log.config.root

import net.kigawa.kodel.api.log.config.LoggerConfigureDsl
import kotlin.reflect.KClass

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class RootLoggerConfigureDsl: LoggerConfigureDsl() {
    @Suppress( "unused")
    fun classConfig(
        clazz: KClass<*>, block: LoggerConfigureDsl.() -> Unit,
    ) {
         child(clazz.qualifiedName!!, block)
    }
}