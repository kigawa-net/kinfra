package net.kigawa.kinfra.infrastructure

import net.kigawa.kinfra.model.sub.SubProject

/**
 * SubProjectの実装クラス
 */
data class SubProjectImpl(
    override val name: String,
    private val relativePath: String = name,
): SubProject {
    override val path: String
        get() = relativePath
}
