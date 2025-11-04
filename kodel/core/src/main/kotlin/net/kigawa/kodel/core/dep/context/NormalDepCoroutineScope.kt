package net.kigawa.kodel.core.dep.context

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.kigawa.kodel.api.dep.context.DepCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

class NormalDepCoroutineScope(
    private val ownCoroutineContext: CoroutineContext,
): DepCoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = ownCoroutineContext + SupervisorJob()

    override fun launch(
        onFail: (e: Exception) -> Unit, onCancel: () -> Unit, onNonComplete: () -> Unit,
        onFinally: () -> Unit,
        block: suspend DepCoroutineScope.() -> Unit,
    ) {
        val job = CoroutineScope(coroutineContext).launch {
            try {
                block(this@NormalDepCoroutineScope)
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                onFail(e)
            }
        }
        CoroutineScope(coroutineContext).launch(start = CoroutineStart.ATOMIC) {
            job.join()
            if (job.isCancelled) onCancel()
            if (!job.isCompleted) onNonComplete()
            onFinally()
        }
    }

    fun plus(depCoroutineScope: NormalDepCoroutineScope): NormalDepCoroutineScope {
        return NormalDepCoroutineScope(ownCoroutineContext + depCoroutineScope.ownCoroutineContext + SupervisorJob())
    }

    fun newScope(): NormalDepCoroutineScope {
        return NormalDepCoroutineScope(coroutineContext)
    }
}