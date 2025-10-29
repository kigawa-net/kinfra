package net.kigawa.kinfra.action.entrypoint

import net.kigawa.kinfra.model.input.KinfraInput
import net.kigawa.kodel.entrypoint.EntrypointGroupBase
import net.kigawa.kodel.entrypoint.EntrypointInfo
import net.kigawa.kodel.err.Res

class RootEntrypoints: EntrypointGroupBase<KinfraInput, Res<Unit, Nothing>>() {
    override val info: EntrypointInfo
        get() = EntrypointInfo(
            "kinfra",
            listOf(),
            "Kinfra command line tool"
        )

    override fun access(input: KinfraInput): Res<Unit, Nothing> {
        return Res.Ok(Unit)
    }
}