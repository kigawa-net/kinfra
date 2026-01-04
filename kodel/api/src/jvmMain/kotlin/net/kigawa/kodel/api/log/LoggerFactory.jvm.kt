package net.kigawa.kodel.api.log

import kotlin.reflect.KClass

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object LoggerFactory: LoggerFactoryCommon() {
    init {
        System.getProperty("jdk.logger.packages")
            ?.let { "$it," }
            .let { it ?: "" }
            .let {
                System.setProperty("jdk.logger.packages", it + "net.kigawa.kodel.api.log.traceignore")
            }
    }

    fun get(clazz: KClass<*>): Kogger = get(clazz.qualifiedName!!)
    actual override fun configureRoot() {
        get("").apply {
            handlers.forEach {
                removeHandler(it)
            }
        }
    }
}
