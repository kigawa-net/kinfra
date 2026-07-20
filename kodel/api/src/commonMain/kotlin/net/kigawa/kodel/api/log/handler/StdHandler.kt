package net.kigawa.kodel.api.log.handler

import net.kigawa.kodel.api.log.config.handler.HandlerConfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class StdHandler(handlerConfig: HandlerConfig): LoggerHandler
