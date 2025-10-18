package net.kigawa.kinfra.model

enum class SubActionType(val actionName: String) {
    LIST("list");

    companion object {
        fun fromString(name: String): SubActionType? {
            return entries.find { it.actionName == name }
        }
    }
}