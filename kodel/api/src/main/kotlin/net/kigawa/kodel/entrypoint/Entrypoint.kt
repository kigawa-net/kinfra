package net.kigawa.kodel.entrypoint

interface Entrypoint<in I, out O> {
    val info: EntrypointInfo
    fun access(input: I): O
}