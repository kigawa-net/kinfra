package net.kigawa.kodel.api.log.handler

import net.kigawa.kodel.api.log.Kogger
import net.kigawa.kodel.api.log.LoggerFactory
import net.kigawa.kodel.api.log.config.HandlerConfig
import java.util.logging.Filter
import java.util.logging.Level

class StdHandler(
    config: HandlerConfig,
): LoggerHandler {
    val errHandler = config.configureJvmStreamHandler(System.err).apply {
        filter = Filter { record ->
            record.level.intValue() >= Level.WARNING.intValue()
        }
    }
    val outHandler = config.configureJvmStreamHandler(System.out).apply {
        filter = Filter { record ->
            record.level.intValue() < Level.WARNING.intValue()
        }
    }

    override fun configure(logger: Kogger) {
        logger.addHandler(errHandler)
        logger.addHandler(outHandler)
    }

}