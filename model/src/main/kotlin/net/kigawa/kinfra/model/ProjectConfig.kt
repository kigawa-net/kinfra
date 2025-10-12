package net.kigawa.kinfra.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    val githubRepository: String? = null
)
