package net.kigawa.kodel.api.log

import java.util.logging.Logger

@Suppress(names = ["EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING"])
actual object NativeLoggerAdaptor {
    actual fun getKogger(name: String): Kogger {
        return Logger.getLogger(name)
    }
}