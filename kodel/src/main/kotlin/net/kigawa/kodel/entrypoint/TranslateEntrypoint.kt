package net.kigawa.kodel.entrypoint

class TranslateEntrypoint<in I, out O, in J, out P, T: Entrypoint<J, P>>(
    val entrypoint: T, private val translator: ((J) -> P).(I) -> O,
): Entrypoint<I, O> {
    override val info: EntrypointInfo
        get() = entrypoint.info

    override fun access(input: I): O {
        return object: (J) -> P {
            override fun invoke(p1: J): P {
               return entrypoint.access(p1)
            }
        }.translator(input)
    }
}