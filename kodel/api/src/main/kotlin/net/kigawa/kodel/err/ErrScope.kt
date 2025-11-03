package net.kigawa.kodel.err

class ErrScope<in E: Throwable> {
    fun err(err: E): Nothing = throw err
}

inline fun <T, reified E: Throwable> tryErr(block: context(ErrScope<E>) () -> T): Res<T, E> {
    return ErrScope<E>().let {
        try {
            Res.Ok(block(it))
        } catch (e: Throwable) {
            if (e !is E) throw e
            Res.Err(e)
        }
    }
}