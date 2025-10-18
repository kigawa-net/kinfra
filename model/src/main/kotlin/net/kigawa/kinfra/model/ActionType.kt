package net.kigawa.kinfra.model

enum class ActionType(val actionName: String) {
    FMT("fmt"),
    VALIDATE("validate"),
    STATUS("status"),
    LOGIN("login"),
    SETUP_R2("setup-r2"),
    SETUP_R2_SDK("setup-r2-sdk"),
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
    CONFIG_EDIT("config");

    companion object {
        fun fromString(name: String): ActionType? {
            return entries.find { it.actionName == name }
        }
    }
}
