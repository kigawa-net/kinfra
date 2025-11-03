package net.kigawa.kodel.entrypoint

abstract class EntrypointGroupBase<I, O>: Entrypoint<I, O> {
    private var subEntrypoints = mutableListOf<Entrypoint<I, O>>()
    val entrypoints: List<Entrypoint<I, O>>
        get() = subEntrypoints


    fun <J, P, T: Entrypoint<J, P>> add(endpoint: T, translator: ((J)->P).(I) -> O): T {
        return endpoint.also { subEntrypoints += TranslateEntrypoint(endpoint, translator) }
    }
}