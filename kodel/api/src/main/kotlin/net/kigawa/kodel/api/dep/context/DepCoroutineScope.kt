package net.kigawa.kodel.api.dep.context

import kotlin.coroutines.CoroutineContext

interface DepCoroutineScope {
    val coroutineContext: CoroutineContext
    fun launch(
        onFail: (e: Exception) -> Unit = {
            println("Error in ${DepCoroutineScope::launch}: ${it.message}")
            it.printStackTrace()
        },
        onCancel: () -> Unit = {},
        onNonComplete: () -> Unit = {},
        onFinally: () -> Unit = {},
        block: suspend DepCoroutineScope.() -> Unit,
    )
}