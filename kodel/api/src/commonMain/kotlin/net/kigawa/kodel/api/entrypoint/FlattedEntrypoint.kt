package net.kigawa.kodel.api.entrypoint

import net.kigawa.kodel.api.log.LoggerFactory
import net.kigawa.kodel.api.log.traceignore.debug

open class FlattedEntrypoint<in I, out O, C>(
    val path: List<EntrypointInfo>,
    val entrypoint: EntrypointNode<I, O, C>,
): EntrypointNode<I, O, C> {
    val logger = LoggerFactory.get("net.kigawa.kodel.api.entrypoint.FlattedEntrypoint")
    override fun access(input: I, ctx: C): O? {
        logger.debug("Accessing flatted entrypoint: ${path.joinToString("/") { it.name.raw }}")
        logger.debug("Input: $input")
        logger.debug("Context: $ctx")
        logger.debug("entrypoint: $entrypoint")
        return entrypoint.access(input, ctx)
    }

    override fun flat(): List<FlattedEntrypoint<I, O, C>> {
        return listOf(this)
    }
}
