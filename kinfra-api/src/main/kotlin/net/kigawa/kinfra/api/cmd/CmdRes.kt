package net.kigawa.kinfra.api.cmd

import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer

interface CmdRes {
    suspend fun <R> writer(block: suspend Writer.() -> R): R
    suspend fun <R> reader(block: suspend Reader<String>.() -> R): R
}