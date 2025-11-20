package net.kigawa.kinfra.api.hash

import net.kigawa.kinfra.api.resource.KinfraResource

data class HashSrc(
    val strs: List<String?> = emptyList(),
    val resources: List<KinfraResource?> = emptyList(),
    val blocks: List<suspend (Hasher) -> Unit> = emptyList(),
) {
    companion object {
        fun str(vararg str: String?) = HashSrc(strs = str.toList())
        fun resource(vararg resource: KinfraResource?) = resource(resource.toList())
        fun resource(resource: List<KinfraResource?>) = HashSrc(resources = resource.toList())
        fun block(block: suspend (Hasher) -> Unit) = HashSrc(blocks = listOf(block))
    }

    fun str(vararg str: String?) = copy(strs = this@HashSrc.strs + str.toList())
    fun resource(vararg resource: KinfraResource) = resource(resource.toList())
    fun resource(resource: List<KinfraResource>) = copy(resources = this@HashSrc.resources + resource)
    fun block(block: suspend (Hasher) -> Unit) = copy(blocks = this@HashSrc.blocks + block)
}