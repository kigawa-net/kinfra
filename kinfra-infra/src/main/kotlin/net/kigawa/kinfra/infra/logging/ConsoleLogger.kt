package net.kigawa.kinfra.infra.logging

import net.kigawa.kodel.api.log.Kogger

//class ConsoleLogger(
//    val jvmLogger: java.util.logging.Logger,
//): Kogger {
//    override fun debug(message: String) {
//        jvmLogger.fine(message)
//    }
//
//    override fun info(message: String) {
//        jvmLogger.info(message)
//    }
//
//    override fun warn(message: String) {
//        jvmLogger.warning(message)
//    }
//
//    override fun error(message: String) {
//        jvmLogger.severe(message)
//    }
//
//    override fun error(message: String, throwable: Throwable) {
//        jvmLogger.severe(message)
//    }
//}