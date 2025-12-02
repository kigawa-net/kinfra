package net.kigawa.kinfra.api.io

interface Reader<T: Any> {
    suspend fun read(): T?
    suspend fun <U: Any, R> map(translate: suspend (T) -> U, block: suspend Reader<U>.() -> R): R {
        return block(MapReader(translate, this))
    }

    suspend fun <U: Any, R> flatMap(translate: suspend (T) -> List<U>, block: suspend Reader<U>.() -> R): R {
        return block(FlatMapReader(translate, this))
    }

    suspend fun forEach(block: suspend (T) -> Unit) {
        while (true) {
            val v = read() ?: break
            block(v)
        }
    }

    suspend fun toList(): List<T> {
        val list = mutableListOf<T>()
        forEach { list.add(it) }
        return list
    }
}