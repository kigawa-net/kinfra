package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.conf.global.GlobalConfig
import net.kigawa.kinfra.model.conf.global.IncompleteGlobalConfig

interface GlobalConfigCompleter {
    fun complete(incompleteGlobalConfig: IncompleteGlobalConfig): GlobalConfig
}