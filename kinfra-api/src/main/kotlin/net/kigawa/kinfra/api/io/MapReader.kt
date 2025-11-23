package net.kigawa.kinfra.api.io

class MapReader<F: Any, T: Any>(
    val translater: suspend (F) -> T,
    val reader: Reader<F>,
): Reader<T> {
    override suspend fun read(): T? {
        return reader.read()?.let { translater.invoke(it) }
    }
}