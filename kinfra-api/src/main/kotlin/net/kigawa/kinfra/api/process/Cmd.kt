package net.kigawa.kinfra.api.process

interface Cmd {
    fun strCmd(): String {
        return raw.joinToString(separator = "' '", prefix = "'", postfix = "'") {
            it.replace("'", "\"'\"")
        }
    }

    val raw: List<String>
}