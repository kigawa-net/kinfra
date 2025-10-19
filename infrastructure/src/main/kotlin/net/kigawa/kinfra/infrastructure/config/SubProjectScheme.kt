package net.kigawa.kinfra.infrastructure.config

import kotlinx.serialization.Serializable
import net.kigawa.kinfra.model.conf.SubProject

/**
 * Serializable implementation of SubProject
 */
@Serializable
data class SubProjectScheme(
    val name: String,
    val path: String = name
) {
    companion object {
        fun from(subProject: SubProject): SubProjectScheme {
            return SubProjectScheme(
                name = subProject.name,
                path = subProject.path
            )
        }
    }

    fun toSubProject(): SubProject {
        return SubProject(
            name = name,
            path = path
        )
    }
}