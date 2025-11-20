package net.kigawa.kinfra.infra.logging

import net.kigawa.kinfra.model.logging.LogLevel
import net.kigawa.kinfra.model.logging.Logger

// Re-export from action module
@Deprecated("use net.kigawa.kinfra.model.logging.Logger")
typealias Logger = Logger
@Deprecated("use net.kigawa.kinfra.model.logging.LogLevel")
typealias LogLevel = LogLevel
