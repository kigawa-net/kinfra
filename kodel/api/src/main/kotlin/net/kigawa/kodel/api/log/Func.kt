package net.kigawa.kodel.api.log

import java.util.logging.Logger
import kotlin.reflect.KClass

fun <T: Any> KClass<T>.getLogger(): Logger {
    return LoggerFactory.get(this)
}

fun <T: Any> T.getLogger(): Kogger {
    return LoggerFactory.get(this::class)
}
