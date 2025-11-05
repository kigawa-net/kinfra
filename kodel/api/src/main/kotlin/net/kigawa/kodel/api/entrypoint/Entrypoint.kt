package net.kigawa.kodel.api.entrypoint

interface Entrypoint<in I, out O> {
    val info: EntrypointInfo

    fun access(input: I): O
}
