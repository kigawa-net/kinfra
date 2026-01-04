package net.kigawa.kodel.api.log

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object LoggerFactory: LoggerFactoryCommon {
    override fun configureRoot()
}