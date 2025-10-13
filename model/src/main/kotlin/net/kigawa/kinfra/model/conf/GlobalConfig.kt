package net.kigawa.kinfra.model.conf

import kotlinx.serialization.Serializable

@Serializable
data class GlobalConfig(
    val login: LoginConfig? = null,
)

@Serializable
data class LoginConfig(
    val repo: String,
)