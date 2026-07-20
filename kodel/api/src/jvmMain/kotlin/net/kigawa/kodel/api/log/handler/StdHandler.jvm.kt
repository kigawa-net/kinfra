package net.kigawa.kodel.api.log.handler

import net.kigawa.kodel.api.log.config.handler.HandlerConfig
import java.util.logging.Filter
import java.util.logging.Level
import java.util.logging.Logger

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual class StdHandler actual constructor(
    handlerConfig: HandlerConfig,
): LoggerHandler {
    val errHandler = handlerConfig.configureJvmStreamHandler(System.err).apply {
        filter = Filter { record ->
            record.level.intValue() >= Level.WARNING.intValue()
        }
    }
    val outHandler = handlerConfig.configureJvmStreamHandler(System.out).apply {
        filter = Filter { record ->
            record.level.intValue() < Level.WARNING.intValue()
        }
    }
    override fun configureJvmStreamHandler(logger: Logger){
        logger.addHandler(errHandler)
        logger.addHandler(outHandler)
    }

}
