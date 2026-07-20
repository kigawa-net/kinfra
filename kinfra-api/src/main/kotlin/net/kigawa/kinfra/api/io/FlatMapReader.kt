package net.kigawa.kinfra.api.io

class FlatMapReader<F: Any, T: Any>(
    val translate: suspend (F) -> List<T>,
    val reader: Reader<F>,
): Reader<T> {
    var latest = mutableListOf<T>()
    override suspend fun read(): T? {
        while (latest.isEmpty()) {
            latest = reader.read()
                ?.let { translate(it) }
                ?.toMutableList()
                ?: return null
        }
        return latest.removeFirst()
    }
}