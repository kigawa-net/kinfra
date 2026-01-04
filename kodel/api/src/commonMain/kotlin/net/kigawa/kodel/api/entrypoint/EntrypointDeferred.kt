package net.kigawa.kodel.api.entrypoint

@Suppress("unused")
class EntrypointDeferred<R>(
    val block: suspend () -> R,
) {
    suspend fun execute(): R = block()
}
