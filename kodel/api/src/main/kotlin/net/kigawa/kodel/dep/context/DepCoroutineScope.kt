package net.kigawa.kodel.dep.context

interface DepCoroutineScope {
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