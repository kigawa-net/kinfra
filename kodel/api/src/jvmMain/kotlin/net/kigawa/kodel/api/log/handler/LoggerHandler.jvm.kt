package net.kigawa.kodel.api.log.handler

import java.util.logging.Logger

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual interface LoggerHandler{
    fun configureJvmStreamHandler(logger: Logger)
}
