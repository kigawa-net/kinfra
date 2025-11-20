package net.kigawa.kinfra.api.io

class FlatMapReader<F, T>(
    val translate: suspend (F) -> List<T>,
    val reader: Reader<F>,
): Reader<T> {
    var latest = mutableListOf<T>()
    override suspend fun read(): T {
        while (latest.isEmpty()) {
            latest = translate(reader.read()).toMutableList()
        }
        return latest.removeFirst()
    }

    override suspend fun hasNext(): Boolean {
        if (latest.isNotEmpty()) return true
        return reader.hasNext()
    }
}