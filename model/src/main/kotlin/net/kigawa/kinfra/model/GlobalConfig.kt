package net.kigawa.kinfra.model

import kotlinx.serialization.Serializable

@Serializable
data class GlobalConfig(
    val githubRepository: String? = null
)
