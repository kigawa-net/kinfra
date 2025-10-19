package net.kigawa.kinfra.model

enum class SubActionType(val actionName: String) {
    LIST("list"),
    ADD("add"),
    SHOW("show");

    companion object {
        fun fromString(name: String): SubActionType? {
            return entries.find { it.actionName == name }
        }
    }
}