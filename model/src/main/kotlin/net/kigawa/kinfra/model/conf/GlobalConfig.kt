package net.kigawa.kinfra.model.conf

interface GlobalConfig {
    val login: LoginConfig?
}

interface LoginConfig {
    val repo: String
    val enabledProjects: List<String>
}