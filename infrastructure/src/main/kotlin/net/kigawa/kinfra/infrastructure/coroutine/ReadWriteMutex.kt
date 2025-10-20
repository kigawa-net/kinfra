package net.kigawa.kinfra.infrastructure.coroutine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class ReadWriteMutex {
    private val readLockCnt = MutableStateFlow(0)
    private val writeMutex = Mutex()

    @OptIn(ExperimentalContracts::class)
    suspend fun <R> readLock(block: suspend () -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return try {
            if (readLockCnt.getAndUpdate { it + 1 } == 0) writeMutex.lock()
            block()
        } finally {
            if (readLockCnt.updateAndGet { it - 1 } == 0) writeMutex.unlock()
        }
    }

    @OptIn(ExperimentalContracts::class)
    suspend fun <R> writeLock(block: suspend () -> R): R {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
        return writeMutex.withLock { block() }
    }
}