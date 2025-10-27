package net.kigawa.kinfra.model

enum class ActionType(val actionName: String) {
    FMT("fmt"),
    VALIDATE("validate"),
    STATUS("status"),
    LOGIN("login"),
    INIT("init"),
    PLAN("plan"),
    APPLY("apply"),
    DESTROY("destroy"),
    DEPLOY("deploy"),
    DEPLOY_SDK("deploy-sdk"),
    HELP("help"),
    HELLO("hello"),
    SELF_UPDATE("self-update"),
    PUSH("push"),
    CONFIG("config"),
    CONFIG_EDIT("config-edit"),
    CURRENT("current"),
    SUB("sub"),
    NEXT("next"),
    SUBMODULE("submodule");

    companion object {
        fun fromString(name: String): ActionType? {
            return entries.find { it.actionName == name }
        }
    }
}
