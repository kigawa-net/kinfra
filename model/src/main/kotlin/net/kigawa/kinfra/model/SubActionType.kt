package net.kigawa.kinfra.model

enum class SubActionType(val actionName: String) {
    LIST("list"),
    ADD("add"),
    SHOW("show"),
    EDIT("edit"),
    REMOVE("rm"),
    PLAN("plan"),
    GENERATE("generate");

    companion object {
        fun fromString(name: String): SubActionType? {
            return entries.find { it.actionName == name }
        }
    }
}