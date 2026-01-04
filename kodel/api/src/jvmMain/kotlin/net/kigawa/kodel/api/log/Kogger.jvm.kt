@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package net.kigawa.kodel.api.log

import net.kigawa.kodel.api.log.handler.LoggerHandler
import java.util.logging.Logger
import kotlin.reflect.KClass

actual typealias Kogger = Logger

actual var Kogger.logLevel: LogLevel
    get() = LogLevel.fromJvm(level)
    set(value) {
        level = value.primary
    }

actual fun Kogger.removeAllHandlers() {
    handlers.forEach { removeHandler(it) }
}

actual fun Kogger.addHandler(handler: LoggerHandler) {
    handler.configureJvmStreamHandler(this)
}

@Suppress("unused")
fun <T: Any> KClass<T>.getKogger(): Kogger {
    return LoggerFactory.get(this)
}

@Suppress("unused")
fun <T: Any> T.getKogger(): Kogger {
    return LoggerFactory.get(this::class)
}
