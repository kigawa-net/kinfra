package net.kigawa.kodel.api.log.handler

import net.kigawa.kodel.api.log.Kogger

interface LoggerHandler {
    fun configure(logger: Kogger)
}