package net.kigawa.kodel.err

sealed interface Res<T, in E: Throwable> {
    class Ok<T, E: Throwable>(val value: T): Res<T, E>
    class Err<T, E: Throwable>(val err: E): Res<T, E>
}