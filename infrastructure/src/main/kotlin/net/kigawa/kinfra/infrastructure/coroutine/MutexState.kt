package net.kigawa.kinfra.infrastructure.coroutine

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class MutexState<T>(
    private var value: T,
) {
    val mutex = ReadWriteMutex()

    @OptIn(ExperimentalContracts::class)
    suspend inline fun <R> writeLock(crossinline block: suspend MutableMutexStateValue<T>.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return mutex.writeLock {
            MutableMutexStateValue(this).block()
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend inline fun <R> readLock(crossinline block: suspend MutexStateValue<T>.() -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return mutex.readLock {
            MutexStateValue(this).block()
        }
    }

    open class MutexStateValue<T>(
        protected val mutexState: MutexState<T>,
    ) {
        open val value: T
            get() = mutexState.value
    }

    class MutableMutexStateValue<T>(mutexState: MutexState<T>) : MutexStateValue<T>(mutexState) {
        override var value: T
            get() = mutexState.value
            set(value) {
                mutexState.value = value
            }
    }

}