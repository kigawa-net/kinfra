package net.kigawa.kinfra.model

interface Action {
    fun execute(args: Array<String>): Int
    fun getDescription(): String
}
