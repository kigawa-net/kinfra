package net.kigawa.kodel.api.log.handler

import net.kigawa.kodel.api.log.LogLevel
import net.kigawa.kodel.api.log.config.handler.HandlerConfig

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class StdHandler actual constructor(handlerConfig: HandlerConfig):
    LoggerHandler {
    override fun log(msg: String, logLevel: LogLevel) {
        println(msg)
    }
}
